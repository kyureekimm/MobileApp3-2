package ddwu.com.mobile.lbstest

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import ddwu.com.mobile.lbstest.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var binding: ActivityMainBinding

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest : LocationRequest
    lateinit var locationCallback : LocationCallback

    val geocoder : Geocoder by lazy {
        Geocoder(this, Locale.getDefault())
    }

    private val sharedPreferences by lazy {
        getSharedPreferences("location_prefs", MODE_PRIVATE)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkPermissions()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest =  LocationRequest.Builder(3000)
            .setMinUpdateIntervalMillis(2000)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLoc: Location = locationResult.locations[0]
                val newLocationText = "위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}"
                Log.d(TAG, newLocationText)
                binding.tvLocation.text = newLocationText

                with(sharedPreferences.edit()) {
                    putString("lastLat", currentLoc.latitude.toString())
                    putString("lastLon", currentLoc.longitude.toString())
                    apply()
                }
            }
        }

        loadSavedAddress()

        binding.btnStart.setOnClickListener {
            Log.d(TAG, "위치 조사를 시작합니다.")
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper() )
        }

        binding.btnStop.setOnClickListener {
            Log.d(TAG, "위치 조사를 종료합니다.")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun loadSavedAddress() {
        val latString = sharedPreferences.getString("lastLat", null)
        val lonString = sharedPreferences.getString("lastLon", null)

        if (latString != null && lonString != null) {
            binding.tvLocation.text = "저장된 주소 불러오는 중..."
            lifecycleScope.launch {
                val address = getAddress(latString.toDouble(), lonString.toDouble())
                binding.tvLocation.text = address
            }
        } else {
            binding.tvLocation.text = "저장된 위치 없음. (START를 누르세요)"
        }
    }


    private suspend fun getAddress(lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                addresses?.get(0)?.getAddressLine(0) ?: "주소 정보 없음"
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding failed", e)
                "주소 변환 실패"
            }
        }
    }


    private fun checkPermissions() {
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "필요 권한 있음")
        } else {
            locationPermissionRequest.launch(
                arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
            )
        }
    }

    val locationPermissionRequest =
        registerForActivityResult( ActivityResultContracts.RequestMultiplePermissions(), {
                permissions ->
            when {
                permissions.getOrDefault(ACCESS_FINE_LOCATION, false) -> {
                    Log.d(TAG, "정확한 위치 사용")
                }
                permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG, "근사 위치 사용")
                }
                else -> {
                    Log.d(TAG, "권한 미승인")
                }
            }
        } )
}