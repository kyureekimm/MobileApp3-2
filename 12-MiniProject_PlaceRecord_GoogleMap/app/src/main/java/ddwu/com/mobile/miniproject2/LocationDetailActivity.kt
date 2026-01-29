package ddwu.com.mobile.miniproject2

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import ddwu.com.mobile.miniproject2.data.MyLoc
import ddwu.com.mobile.miniproject2.data.MyLocDao
import ddwu.com.mobile.miniproject2.data.MyLocDatabase
import ddwu.com.mobile.miniproject2.databinding.ActivityLocationDetailBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


//(2-1)
class LocationDetailActivity : AppCompatActivity() {

    val TAG = "MINI_PROJECT_DETAIL"

    val binding by lazy { ActivityLocationDetailBinding.inflate(layoutInflater) }

    val geocoder: Geocoder by lazy {
        Geocoder(this, Locale.getDefault())
    }

    //(3) myLocDao 객체 생성
    lateinit var myLocDao: MyLocDao


    //(5)
    // 수정 모드인지 판별할 변수 (0이면 신규, 아니면 수정)
    var updateId = 0
    var lat = 0.0
    var lng = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //DB 초기화
        val db = MyLocDatabase.getInstance(this)
        myLocDao = db.myLocDao()

        //Intent 데이터 확인 (목록에서 왔는지, 지도에서 왔는지??)
        updateId = intent.getIntExtra("id", 0)

        if (updateId == 0) {
            //[신규 추가 모드] 지도에서 핀 찍고 들어옴
            lat = intent.getDoubleExtra("lat", 0.0)
            lng = intent.getDoubleExtra("lng", 0.0)
            if (lat != 0.0 && lng != 0.0) {
                getAddress(lat, lng)
            }
        } else {
            // [수정 모드] 목록 클릭해서 들어옴 -> DB에서 내용 가져오기
            binding.btnDetailSave.text = "수정"
            getLocById(updateId)
        }

//        val lat = intent.getDoubleExtra("lat", 0.0)
//        val lng = intent.getDoubleExtra("lng", 0.0)
//
//        if(lat != 0.0 && lng != 0.0){
//            getAddress(lat, lng)
//        } else {
//            Toast.makeText(this, "위치 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
//        }
//
//        //(3)
//        val myLocDB : MyLocDatabase = Room.databaseBuilder(
//            applicationContext, MyLocDatabase::class.java, "myLoc_db"
//        ).build()
//
//        myLocDao = myLocDB.myLocDao()


        //(3-1) 저장버튼 구현. 야미
        binding.btnDetailSave.setOnClickListener {
            // 여기에 DB 저장 로직이 들어갈 예정 (제목: etLocTitle.text, 주소: etLocAddress.text 등)
            //Toast.makeText(this, "저장 버튼 클릭됨 (기능 미구현)", Toast.LENGTH_SHORT).show()
            //finish()

            val title = binding.etLocTitle.text.toString()
            val address = binding.etLocAddress.text.toString()
            val memo = binding.etLocMemo.text.toString()

            //제목이 비어있으면 저장 안함
            if (title.isBlank()) {
                Toast.makeText(this, "장소명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //저장할 데이터 객체(Entity)생성
            val myLoc = MyLoc(
                _id = updateId,
                locTitle = title,
                locAddress = address,
                locMemo = memo,
                locLat = lat,
                locLng = lng
            )
            CoroutineScope(Dispatchers.IO).launch {
//                myLocDao.insertLoc(newLoc)
//                Log.d(TAG, "DB저장 성공: $title")
//
//                //UI 작업은 메인스레드에서! (종료작업?)
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(this@LocationDetailActivity, "위치가 저장되었습니다.", Toast.LENGTH_SHORT).show()
//                    finish()
//                }

                //(5)
                if (updateId == 0) {
                    myLocDao.insertLoc(myLoc) // 저장
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "추가되었습니다", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    myLocDao.updateLoc(myLoc) // 수정
                    withContext(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "수정되었습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            }
        }


        binding.btnDetailCancel.setOnClickListener {
            finish()
        }
    }

//    private fun getAddress(lat: Double, lng: Double) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val addresses = geocoder.getFromLocation(lat, lng, 1)
//
//            if(!addresses.isNullOrEmpty()){
//                val addressStr = addresses[0].getAddressLine(0)
//                Log.d(TAG, "찾은 주소: $addressStr")
//
//                withContext(Dispatchers.Main) {
//                    binding.etLocAddress.setText(addressStr)
//                }
//            }
//        }
//    }

    //5
    // ID로 DB 조회해서 화면에 뿌리기
    private fun getLocById(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val loc = myLocDao.getLocById(id)
            // 좌표값 유지
            lat = loc.locLat ?: 0.0
            lng = loc.locLng ?: 0.0

            withContext(Dispatchers.Main) {
                binding.etLocTitle.setText(loc.locTitle)
                binding.etLocAddress.setText(loc.locAddress)
                binding.etLocMemo.setText(loc.locMemo)
            }
        }
    }

    // 좌표로 주소 찾기
    private fun getAddress(lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        binding.etLocAddress.setText(addresses[0].getAddressLine(0))
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}