package ddwu.com.mobile.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.media.ExifInterface
import android.os.Bundle
import com.bumptech.glide.request.transition.Transition
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ddwu.com.mobile.finalproject.data.MusicSpot
import ddwu.com.mobile.finalproject.data.MusicSpotDao
import ddwu.com.mobile.finalproject.data.MusicSpotDatabase
import ddwu.com.mobile.finalproject.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "MusicSpot_Main"
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var musicSpotDao: MusicSpotDao

    private val geocoder: Geocoder by lazy { Geocoder(this, Locale.getDefault()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        musicSpotDao = MusicSpotDatabase.getInstance(this).musicSpotDao()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.btnCurrentLoc.setOnClickListener {
            //검색창 글자 지우기!!
            binding.etSearchAddr.text.clear()
            checkPermissionsAndGetLocation()
        }


        binding.btnLocList.setOnClickListener {
            binding.etSearchAddr.text.clear()
            val intent = Intent(this, SpotListActivity::class.java)
            startActivity(intent)
        }


        binding.btnSearchAddr.setOnClickListener {
            val address = binding.etSearchAddr.text.toString()
            if (address.isNotBlank()) {
                searchLocation(address)
            } else {
                Toast.makeText(this, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        checkPermissions()
    }


    private fun searchLocation(address: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {

                val addresses = geocoder.getFromLocationName(address, 1)

                if (!addresses.isNullOrEmpty()) {
                    val loc = addresses[0]
                    val latLng = LatLng(loc.latitude, loc.longitude)


                    withContext(Dispatchers.Main) {
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "장소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d(TAG, "GoogleMap 준비 완료")


        googleMap.setOnMapLongClickListener { latLng ->
            binding.etSearchAddr.text.clear()

            moveToAddSpot(latLng.latitude, latLng.longitude)
        }

        // 1. 마커 클릭 이벤트
        googleMap.setOnMarkerClickListener { marker ->
            val spotId = marker.tag as? Int
            if (spotId != null) {
                showPreviewCard(spotId)
            }
            false
        }

        googleMap.setOnMapClickListener {
            binding.cvSpotPreview.visibility = android.view.View.GONE
        }


        observeAndShowMarkers()


        setInitialLocation()
    }

    private fun showPreviewCard(id: Int) {
        lifecycleScope.launch {

            musicSpotDao.getSpotById(id).collect { spot ->
                spot?.let {
                    binding.tvPreviewTitle.text = it.songTitle
                    binding.tvPreviewArtist.text = it.artistName


                    if (it.memo.isNullOrBlank()) {
                        binding.tvPreviewMemo.text = "기록된 메모가 없습니다."
                    } else {
                        binding.tvPreviewMemo.text = it.memo
                    }

                    if (!it.photoPath.isNullOrEmpty()) {

                        val rotatedBitmap = getRotatedBitmap(it.photoPath!!)
                        binding.ivPreviewAlbum.setImageBitmap(rotatedBitmap)
                    } else {
                        Glide.with(this@MainActivity)
                            .load(it.albumUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .into(binding.ivPreviewAlbum)
                    }

                    binding.cvSpotPreview.visibility = android.view.View.VISIBLE

                    binding.cvSpotPreview.setOnClickListener {
                        val intent = Intent(this@MainActivity, SpotDetailActivity::class.java)
                        intent.putExtra("spotId", id)
                        startActivity(intent)
                    }
                }
            }
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

    private fun setInitialLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
                }
            }
        }
    }

    //마커
    private fun observeAndShowMarkers() {
        lifecycleScope.launch {
            musicSpotDao.getAllSpots().collect { spotList ->
                Log.d(TAG, "DB에서 읽어온 데이터 개수: ${spotList.size}")
                googleMap.clear()

                spotList.forEach { spot ->
                    val position = LatLng(spot.locLat ?: 0.0, spot.locLng ?: 0.0)
                    Log.d(TAG, "마커 생성 시도: ${spot.songTitle} (${position.latitude}, ${position.longitude})")


                    val marker = googleMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(spot.songTitle)
                            .snippet(spot.artistName)
                    )
                    marker?.tag = spot.id


                    if (marker != null && !spot.albumUrl.isNullOrEmpty()) {
                        updateMarkerIcon(marker, spot.albumUrl!!)
                    }
                }
            }
        }
    }

    //기존 마커 아이콘 변경
    private fun updateMarkerIcon(marker: Marker, url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .override(120, 120)
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(resource))
                }
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    Log.e(TAG, "마커 이미지 로드 실패: $url")
                }
            })
    }
    private fun checkPermissionsAndGetLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {

                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))

                    Log.d(TAG, "현재 위치로 지도 이동 완료")
                } else {
                    Toast.makeText(this, "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            checkPermissions()
        }
    }


    private fun moveToAddSpot(lat: Double, lng: Double) {
        val intent = Intent(this, AddSpotActivity::class.java)
        intent.putExtra("lat", lat)
        intent.putExtra("lng", lng)
        startActivity(intent)
    }

    private fun checkPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            Log.d(TAG, "권한 승인됨")
        }
    }
}