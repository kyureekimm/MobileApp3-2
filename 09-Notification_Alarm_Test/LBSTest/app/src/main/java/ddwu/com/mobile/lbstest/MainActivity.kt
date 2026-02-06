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
import ddwu.com.mobile.lbstest.databinding.ActivityMainBinding
import com.google.android.gms.location.*
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

        checkPermissions()      // 위치 요청 권한 확인

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest =  LocationRequest.Builder(5000) // 수신 간격
            .setMinUpdateIntervalMillis(3000)  // 최소 수신 간격
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)  // 수신 우선순위
            .build()


        locationCallback =object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLoc: Location = locationResult.locations[0]
                Log.d(TAG, "위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}")
            }
        }


        binding.btnStart.setOnClickListener {
            // 위치 조사 시작
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper() )

        }

        binding.btnStop.setOnClickListener {
            // 위치 조사 종료
            Log.d(TAG, "위치 조사를 종료합니다.")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        binding.btnLastLoc.setOnClickListener {
            getLastLocation()
        }


        binding.btnGeo.setOnClickListener {
            // Geocoding 실행
            geocoder.getFromLocationName("동덕여자대학교", 1) {
                    address ->
                binding.etAddress.setText("${address[0].latitude}, ${address[0].longitude}") }
        }

        binding.btnReverseGeo.setOnClickListener {
            // Reverse geocoding 실행
            geocoder.getFromLocation(37.606358, 127.041822, 5) {
                    addresses ->
                binding.etAddress.setText("${addresses.get(0).getAddressLine(0)}")
            }
        }

        binding.btnMap.setOnClickListener {
            callExternalMap()
        }
    }

    /*사용자가 위치 조사를 종료하지 않았을 경우를 위해 추가*/
    override fun onPause() {
        super.onPause()
        // 위치 조사 종료
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /*최종 위치 확인*/
    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
                location : Location? ->
            Log.d(TAG, "최종 위치 확인: ${location?.latitude}, ${location?.longitude}")
        }
        fusedLocationClient.lastLocation.addOnFailureListener {
            Log.d(TAG, "최종 위치 확인 실패")
        }
    }

    /*외주 지도 앱 호출*/
    fun callExternalMap() {
        // 위도/경도 정보로 지도 요청 시
        val locLatLng = String.format("geo:%f,%f?z=%d", 37.606320, 127.041808, 17)
        // 위치명으로 지도 요청 시
        val locName = "https://www.google.co.kr/maps/place/" + "Hawolgok-dong"
        val uri = Uri.parse(locLatLng)
//         val uri = Uri.parse(locName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }


    private fun checkPermissions() {    // 권한 확인이 필요한 곳에서 호출
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "필요 권한 있음")
            // 권한이 이미 있을 경우 필요한 기능 실행
        } else {
            // 권한이 없을 경우 권한 요청
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
                    Log.d(TAG, "정확한 위치 사용") // 정확한 위치 접근 권한 승인거부 후 해야할 작업
                }
                permissions.getOrDefault(ACCESS_COARSE_LOCATION, false) -> {
                    Log.d(TAG, "근사 위치 사용") // 근사 위치 접근 권한 승인 후 해야할 작업
                }
                else -> {
                    Log.d(TAG, "권한 미승인") // 권한 미승인 시 해야 할 작업
                }
            }
        } )
}