package ddwu.com.mobile.miniproject2

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
//import android.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.GeneratedAdapter
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import ddwu.com.mobile.miniproject2.data.MyLocDao
import ddwu.com.mobile.miniproject2.data.MyLocDatabase
import ddwu.com.mobile.miniproject2.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.getValue
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    val TAG = "MINI_PROJECT"
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    lateinit var googleMap: GoogleMap
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    var centerMarker : Marker? = null   // 중심 Marker 보관용
    val geocoder: Geocoder by lazy {
        Geocoder(this, Locale.getDefault())
    }
    lateinit var currentLine: Polyline  // 그리기 선 보관용

    //(4-2)DB접근을 위한 DAO객체
    lateinit var myLocDao : MyLocDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        //(4-2)DB 연결 및 DAO 초기화 (반드시 필요)
//        val db = Room.databaseBuilder(
//            applicationContext,
//            MyLocDatabase::class.java,
//            "myLoc_db"
//        ).build()
//        myLocDao = db.myLocDao()
        // 이렇게 바꿔야 DB가 두 개 안 생김
        val db = MyLocDatabase.getInstance(this)
        myLocDao = db.myLocDao()


        /*위치 확인 관련 코드 작성*/
        checkPermissions()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(5000)
            .setMinUpdateIntervalMillis(3000)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val currentLoc : Location = locationResult.locations[0]
                val targetLoc = LatLng(currentLoc.latitude, currentLoc.longitude)

                Log.d(TAG, "위도: ${currentLoc.latitude}, 경도: ${currentLoc.longitude}")

                //1. GoogleMap 이동
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17f))

                //2. centerMarker 이동 (기존 마커 제거 후 새로 생성하여 이동 효과)
                centerMarker?.remove()
                addCenterMarker(targetLoc)

                //3.위치수신 중단 (한 번만 받고 끝냄미)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }




        /*구글 지도 객체 로딩 코드 작성*/
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync (mapReadyCallback)


        binding.btnCurrentLoc.setOnClickListener {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        //(1-4)
        binding.btnMoveLoc.setOnClickListener {
            val addressStr = binding.etAddress.text.toString()

            if (addressStr.isEmpty()) {
                Log.d(TAG, "주소가 입력되지 않음")
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 주소 검색 (결과 1개만 요청)
                    val addresses = geocoder.getFromLocationName(addressStr, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val lat = addresses[0].latitude
                        val lon = addresses[0].longitude
                        val targetLoc = LatLng(lat, lon)

                        // UI 변경(지도 이동, 마커)은 Main 스레드에서!
                        withContext(Dispatchers.Main) {
                            // 1. 지도 이동
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLoc, 17f))

                            // 2. 마커 생성 (기존에 만드신 함수 활용)
                            addCenterMarker(targetLoc)

                            Log.d(TAG, "성공: $addressStr 위치로 이동 및 마커 생성")
                        }
                    } else {
                        Log.d(TAG, "주소 검색 결과 없음: $addressStr")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "지오코딩 실패: 인터넷 연결이나 API 키를 제발 확인해주삽사리와요...")
                }
            }
        }


        //(2-1)
        binding.btnSaveLoc.setOnClickListener {
//            val intent = Intent(this@MainActivity, LocationDetailActivity::class.java)
//
//            /*Intent에 위도 경도 추가*/
//
//            startActivity(intent)
            // 1. 현재 마커(centerMarker)가 있는지 확인
            if (centerMarker != null) {
                val lat = centerMarker!!.position.latitude
                val lng = centerMarker!!.position.longitude

                // 2. Intent 생성 및 위도/경도 데이터 담기
                val intent = Intent(this@MainActivity, LocationDetailActivity::class.java)
                intent.putExtra("lat", lat)
                intent.putExtra("lng", lng)

                // 3. 화면 이동
                startActivity(intent)
            } else {
                // 마커가 아직 없을 경우 안내
                Toast.makeText(this, "먼저 위치를 확인해주세요.", Toast.LENGTH_SHORT).show()
            }
        }


        //[4-1] [추가한 위치 보기] 버튼 클릭 구현
        /*마커 표시 함수 호출*/
        binding.btnShowMarkers.setOnClickListener {
            readMarker()

        }

        //[4-2] [모든 마커 지우기] 버튼 클릭 구현
        binding.btnClearMarkers.setOnClickListener {
            // 모든 마커 삭제

            // 현재의 centerMarker 위치에 새롭게 centerMarker 추가
            // 1. 현재 centerMarker가 있다면 그 위치를 임시 변수에 저장
            val oldPosition = centerMarker?.position

            if (oldPosition != null) {
                // 2. 맵의 모든 마커와 선(Polyline) 삭제
                googleMap.clear()

                // 3. 아까 저장해둔 위치에 CenterMarker 다시 생성
                addCenterMarker(oldPosition)

                // [중요] clear()는 선(Polyline)도 지워버립니다.
                // 이후에 다시 선을 그릴 수 있도록 Polyline 객체를 다시 초기화해줘야 합니다.
                val polylineOptions = PolylineOptions().apply {
                    color(Color.RED)
                    width(5f)
                }
                currentLine = googleMap.addPolyline(polylineOptions)
            }
        }


        //(5) 위치목록 클릭 구현
        binding.btnLocList.setOnClickListener {
            val intent = Intent(this@MainActivity, LocationsActivity::class.java)
            startActivity(intent)
        }

        // 실행 시 위치서비스 관련 권한 확인
        checkPermissions()
    }


    /*Google Map 설정*/
    val mapReadyCallback = object : OnMapReadyCallback {
        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            Log.d(TAG, "GoogleMap is ready")
            //*fragment 에 기록한 위치로  centerMarker 추가*//*
            googleMap.setOnMapClickListener { latLng: LatLng ->
                Log.d(TAG, "클릭 위치 위도: ${latLng.latitude}, 경도: ${latLng.longitude}")
            }

            googleMap.setOnMapLongClickListener{ latLng : LatLng ->
                Log.d(TAG, "롱클릭 위치 위도: ${latLng.latitude}, 경도: ${latLng.longitude}")
                addMarker(latLng)
            }

            googleMap.setOnMarkerClickListener { marker : Marker ->
                Log.d(TAG, "마커 클릭: ${marker.tag} : (${marker.position}")
                false
            }

            googleMap.setOnInfoWindowClickListener{ marker: Marker ->
                Log.d(TAG, "${marker.id}")
            }

            val polylineOptions = PolylineOptions().apply {
                color(Color.RED)
                width(5f)
            }
            currentLine = googleMap.addPolyline(polylineOptions)


            //*최종 위치 확인 후 해당 위치로 지도 및 centerMarker 이동*//*
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if(location != null) {
                    googleMap.moveCamera(
                        CameraUpdateFactory. newLatLngZoom(
                            LatLng(location.latitude, location.longitude), 17f
                        )
                    )
                    addCenterMarker( LatLng(location.latitude, location.longitude))
                }
            }

            fusedLocationClient.lastLocation.addOnFailureListener {
                val latLng = LatLng(37.606320, 127.041808)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                addCenterMarker(latLng)
            }


        }
    }

    private fun drawLine (latLng: LatLng) {
        val points = currentLine.points
        points.add(latLng)
        currentLine.points = points
    }

    /*Marker 추가 함수*/
    private fun addMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title("Marker Title")
            icon(BitmapDescriptorFactory.fromResource(R.drawable.somsom))
        }

        val currentMarker: Marker? = googleMap.addMarker(markerOptions)
        currentMarker?.tag = "database_id"

        drawLine(latLng)

        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5) { addresses ->
            CoroutineScope(Dispatchers.Main).launch {
                currentMarker?.snippet = addresses[0].getAddressLine(0)
                currentMarker?.showInfoWindow()
            }
        }

    }

    /*centerMarker를 추가하는 함수 구현*/
    private fun addCenterMarker(latLng: LatLng) {
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title("현재 위치")
            icon(BitmapDescriptorFactory.fromResource(R.drawable.somsom))
        }
        centerMarker = googleMap.addMarker(markerOptions)
    }


    /*DB에 저장한 위치 정보를 사용하여 Marker 추가 함수 구현*/
    private fun readMarker() {
        if (googleMap != null) {
            CoroutineScope(Dispatchers.IO).launch {
                // DB의 정보로 Marker 추가 코드 구현
                val locList = myLocDao.getAllLocs()

                withContext(Dispatchers.Main) {
                    for (loc in locList) {
                        if (loc.locLat != null && loc.locLng != null) {
                            val latLng = LatLng(loc.locLat!!, loc.locLng!!)

                            val markerOptions = MarkerOptions().apply {
                                position(latLng)
                                title(loc.locTitle)
                                //snippet(loc.locAddress)
                                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            }

                            googleMap.addMarker(markerOptions)
                        }
                    }
                    Toast.makeText(this@MainActivity, "저장된 위치를 모두 불러왔습니다.", Toast.LENGTH_SHORT).show()
                }

            }
        }
    }



    override fun onPause() {
        super.onPause()
        /*위치 정보 조사 중단 코드 추가*/
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    /*위치 정보 권한 처리*/
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