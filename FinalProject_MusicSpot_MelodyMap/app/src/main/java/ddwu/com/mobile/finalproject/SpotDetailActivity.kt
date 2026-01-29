package ddwu.com.mobile.finalproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import ddwu.com.mobile.finalproject.data.MusicSpot
import ddwu.com.mobile.finalproject.data.MusicSpotDao
import ddwu.com.mobile.finalproject.data.MusicSpotDatabase
import ddwu.com.mobile.finalproject.databinding.ActivitySpotDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SpotDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding by lazy { ActivitySpotDetailBinding.inflate(layoutInflater) }
    private lateinit var musicSpotDao: MusicSpotDao
    private var currentSpot: MusicSpot? = null

    //파일 삭제용
    private val fileUtil by lazy { FileUtil(this) }

    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        musicSpotDao = MusicSpotDatabase.getInstance(this).musicSpotDao()


        val spotId = intent.getIntExtra("spotId", -1)


        if (spotId != -1) {
            loadSpotData(spotId)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.detailMap) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.btnDetailDelete.setOnClickListener {
            showDeleteDialog()
        }

        binding.btnDetailEdit.setOnClickListener {
            val intent = Intent(this, AddSpotActivity::class.java)
            intent.putExtra("spotId", currentSpot?.id)
            startActivity(intent)
        }

        binding.btnPlayMusic.setOnClickListener {
            val artist = currentSpot?.artistName ?: ""
            val title = currentSpot?.songTitle ?: ""

            if (artist.isNotEmpty() && title.isNotEmpty()) {
                val query = "$artist $title"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$query"))

                //유튜브나 브라우저로 실행
                startActivity(intent)
            } else {
                Toast.makeText(this, "노래 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadSpotData(id: Int) {
        lifecycleScope.launch {
            musicSpotDao.getSpotById(id).collect { spot ->
                currentSpot = spot

                spot?.let {

                    binding.tvDetailSong.text = it.songTitle
                    binding.tvDetailArtist.text = it.artistName
                    binding.tvDetailMemo.text = it.memo
                    binding.tvDetailAddress.text = it.locAddress
                    binding.tvDetailDate.text = it.saveDate ?: "날짜 없음"


                    Glide.with(this@SpotDetailActivity)
                        .load(spot.albumUrl)
                        .into(binding.ivDetailAlbumArt)


                    if (!it.photoPath.isNullOrEmpty()) {
                        val rotatedBitmap = getRotatedBitmap(it.photoPath!!)
                        binding.ivDetailPhoto.setImageBitmap(rotatedBitmap)
                    }

                    updateMapMarker(it)
                }
            }
        }
    }


    private fun updateMapMarker(spot: MusicSpot) {

        val mMap = googleMap ?: return

        val lat = spot.locLat ?: 0.0
        val lng = spot.locLng ?: 0.0
        val pos = LatLng(lat, lng)

        mMap.clear() //기존 마커 지우기
        if (lat != 0.0 && lng != 0.0) {
            mMap.addMarker(MarkerOptions().position(pos).title(spot.songTitle))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        currentSpot?.let { spot ->
            updateMapMarker(spot)
        }
    }


    private fun getRotatedBitmap(photoPath: String): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(photoPath) ?: return null

        val ei = ExifInterface(photoPath)
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val angle = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }



    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("기록 삭제")
        builder.setMessage("정말로 이 음악 스팟을 삭제하시겠습니까?")
        builder.setPositiveButton("삭제") { _, _ ->
            deleteSpot()
        }
        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun deleteSpot() {
        currentSpot?.let { spot ->
            CoroutineScope(Dispatchers.IO).launch {

                if (!spot.photoPath.isNullOrEmpty()) {
                    fileUtil.deleteFile(spot.photoPath)
                }


                musicSpotDao.deleteSpot(spot)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SpotDetailActivity, "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}