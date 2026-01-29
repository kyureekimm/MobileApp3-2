package ddwu.com.mobile.miniproject2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import ddwu.com.mobile.miniproject2.data.MyLoc
import ddwu.com.mobile.miniproject2.data.MyLocDao
import ddwu.com.mobile.miniproject2.data.MyLocDatabase
import ddwu.com.mobile.miniproject2.databinding.ActivityLocationsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class LocationsActivity : AppCompatActivity() {

    val binding by lazy { ActivityLocationsBinding.inflate(layoutInflater) }

    /*LocationsAdapter 추가 시 주석 해제*/
    lateinit var locAdapter: LocationAdapter

    lateinit var myLocDao: MyLocDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. DB 초기화 (MainActivity와 동일하게)
        // 이렇게 바꿔야 DB가 두 개 안 생김
        val db = MyLocDatabase.getInstance(this)
        myLocDao = db.myLocDao()

        /*LocationsAdapter 추가 시 주석 해제*/
        //어댑터 설정
        locAdapter = LocationAdapter()
        binding.rvLocations.adapter = locAdapter
        binding.rvLocations.layoutManager = LinearLayoutManager(this)


        // 아이템 클릭 시 상세화면으로 이동 (수정 모드)
        /*DB를 읽어와 Adapter의 list에 추가*/
        locAdapter.setOnLocClickListener(object : LocationAdapter.OnLocClickListener {
            override fun onItemClick(view: View, position: Int) {
                val selectedLoc = locAdapter.locations?.get(position)

                // 클릭한 위치의 ID를 상세화면으로 전달
                val intent = Intent(this@LocationsActivity, LocationDetailActivity::class.java)
                intent.putExtra("id", selectedLoc?._id)
                startActivity(intent)
            }
        })

        //(5)추가. 롱클릭 시 삭제 기능
        locAdapter.setOnLocLongClickListener(object : LocationAdapter.OnLocLongClickListener {
            override fun onItemLongClick(view: View, position: Int) {
                val selectedLoc = locAdapter.locations?.get(position) ?: return

                // 삭제 확인 다이얼로그 띄우기
                AlertDialog.Builder(this@LocationsActivity)
                    .setTitle("삭제 확인")
                    .setMessage("${selectedLoc.locTitle} 항목을 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { _, _ ->
                        // 확인 누르면 삭제 진행
                        deleteLocation(selectedLoc)
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
        })


        // 4. DB 읽어오기 (화면 처음 켜질 때)
        getAllLocations()

        binding.btnLocationsClose.setOnClickListener {
            finish()
        }
    }

    // 화면이 뜰 때마다 목록 갱신 (수정 후 돌아왔을 때 반영됨)
    override fun onResume() {
        super.onResume()
        getAllLocations()
    }

    //목록 조회함수.
    private fun getAllLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            val locList = myLocDao.getAllLocs() // DB에서 가져오기

            withContext(Dispatchers.Main) {
                locAdapter.locations = locList
                locAdapter.notifyDataSetChanged() // 리스트 갱신
            }
        }
    }



    //(5)추가. 항목 삭제함수.
    private fun deleteLocation(loc: MyLoc) {
        CoroutineScope(Dispatchers.IO).launch {
            // DB에서 삭제
            myLocDao.deleteLoc(loc)

            // 삭제 후 목록 새로고침
            withContext(Dispatchers.Main) {
                Toast.makeText(this@LocationsActivity, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                getAllLocations() // 목록 갱신 호출
            }
        }
    }
}