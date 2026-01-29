package ddwu.com.mobile.finalproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.location.Geocoder
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import ddwu.com.mobile.finalproject.data.MusicApiService
import ddwu.com.mobile.finalproject.data.MusicSpot
import ddwu.com.mobile.finalproject.data.MusicSpotDao
import ddwu.com.mobile.finalproject.data.MusicSpotDatabase
import ddwu.com.mobile.finalproject.databinding.ActivityAddSpotBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Locale

class AddSpotActivity : AppCompatActivity() {

    private val TAG = "AddSpotActivity"
    private val binding by lazy { ActivityAddSpotBinding.inflate(layoutInflater) }


    private val geocoder: Geocoder by lazy {
        Geocoder(this, Locale.getDefault())
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://itunes.apple.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(MusicApiService::class.java)

    private lateinit var musicSpotDao: MusicSpotDao

    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var selectedAlbumUrl: String? = null


    private val fileUtil by lazy { FileUtil(this) }
    private var currentPhotoPath: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            //안되먄 메시지 표시
            Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private var editSpotId: Int = -1 // -1이면 새로,, 아니면 수정
    private var currentSpot: MusicSpot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        musicSpotDao = MusicSpotDatabase.getInstance(this).musicSpotDao()


        lat = intent.getDoubleExtra("lat", 0.0)
        lng = intent.getDoubleExtra("lng", 0.0)


        if (lat != 0.0 && lng != 0.0) {
            getAddress(lat, lng)
        }


        binding.btnCancelSpot.setOnClickListener {
            finish()
        }


        binding.btnSongSearch.setOnClickListener {
            val query = binding.etSongSearch.text.toString()
            if (query.isNotBlank()) {
                searchMusic(query)
            }
        }


        binding.btnSaveSpot.setOnClickListener {
            saveMusicSpot()
        }


        binding.btnCapture.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {

                dispatchTakePictureIntent()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        editSpotId = intent.getIntExtra("spotId", -1)

        if (editSpotId != -1) {

            loadExistingData(editSpotId)
            binding.btnSaveSpot.text = "수정 완료"
        }
    }


    private fun getAddress(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val addresses = geocoder.getFromLocation(lat, lng, 1)

                if (!addresses.isNullOrEmpty()) {
                    val addressLine = addresses[0].getAddressLine(0)


                    withContext(Dispatchers.Main) {
                        binding.etSpotAddress.setText(addressLine)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "주소 변환 실패: ${e.message}")
            }
        }
    }

    private fun searchMusic(query: String) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.searchSongs(query)
                if (response.results.isNotEmpty()) {
                    val track = response.results[0]

                    withContext(Dispatchers.Main) {

                        binding.tvSelectedSong.text = track.trackName
                        binding.tvSelectedArtist.text = track.artistName
                        selectedAlbumUrl = track.artworkUrl100


                        Glide.with(this@AddSpotActivity)
                            .load(track.artworkUrl100)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(binding.ivAlbumArt)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveMusicSpot() {
        val title = binding.tvSelectedSong.text.toString()
        val artist = binding.tvSelectedArtist.text.toString()
        val address = binding.etSpotAddress.text.toString()
        val memo = binding.etSpotMemo.text.toString()
        val currentDate = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault()).format(java.util.Date())


        if (title == "선택된 노래 없음" || title.isBlank()) {
            Toast.makeText(this, "노래를 먼저 검색해서 선택해주세요!", Toast.LENGTH_SHORT).show()
            return
        }


        val newSpot = MusicSpot(
            id = if (editSpotId != -1) editSpotId else 0,
            songTitle = title,
            artistName = artist,
            albumUrl = selectedAlbumUrl,
            photoPath = currentPhotoPath,
            locLat = lat,
            locLng = lng,
            locAddress = address,
            memo = memo,
            saveDate = if (editSpotId != -1) currentSpot?.saveDate else currentDate
        )


        CoroutineScope(Dispatchers.IO).launch {
            if (editSpotId != -1) {
                musicSpotDao.updateSpot(newSpot)
            } else {
                musicSpotDao.insertSpot(newSpot)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@AddSpotActivity, "기록이 반영되었습니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            val path = fileUtil.getFilePath()
            currentPhotoPath = path
            File(path)
        } catch (ex: Exception) { null }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "ddwu.com.mobile.finalproject.fileprovider", //주의주의
                it
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureLauncher.launch(intent)
        }
    }


    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {

            binding.ivCapturedPhoto.imageTintList = null


            Glide.with(this)
                .load(currentPhotoPath)
                .centerCrop()
                .error(android.R.drawable.ic_menu_report_image) //에러, 기본
                .into(binding.ivCapturedPhoto)

            Log.d(TAG, "사진 촬영 성공 및 표시 완료: $currentPhotoPath")
        } else {
            fileUtil.deleteFile(currentPhotoPath)
            currentPhotoPath = null
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

    private fun loadExistingData(id: Int) {
        lifecycleScope.launch {
            musicSpotDao.getSpotById(id).collect { spot ->
                currentSpot = spot
                spot?.let {
                    binding.tvSelectedSong.text = it.songTitle
                    binding.tvSelectedArtist.text = it.artistName
                    binding.etSpotAddress.setText(it.locAddress)
                    binding.etSpotMemo.setText(it.memo)


                    selectedAlbumUrl = it.albumUrl

                    currentPhotoPath = it.photoPath
                    lat = it.locLat ?: 0.0
                    lng = it.locLng ?: 0.0


                    Glide.with(this@AddSpotActivity).load(it.albumUrl).into(binding.ivAlbumArt)


                    if (!it.photoPath.isNullOrEmpty()) {
                        binding.ivCapturedPhoto.imageTintList = null
                        Glide.with(this@AddSpotActivity)
                            .load(it.photoPath)
                            .centerCrop()
                            .into(binding.ivCapturedPhoto)
                    }
                }
            }
        }
    }
}