package ddwu.com.mobile.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ddwu.com.mobile.finalproject.data.MusicSpotDao
import ddwu.com.mobile.finalproject.data.MusicSpotDatabase
import ddwu.com.mobile.finalproject.databinding.ActivitySpotListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class SpotListActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySpotListBinding.inflate(layoutInflater) }
    private lateinit var spotAdapter: SpotAdapter
    private lateinit var musicSpotDao: MusicSpotDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        musicSpotDao = MusicSpotDatabase.getInstance(this).musicSpotDao()
        spotAdapter = SpotAdapter()


        binding.rvSpots.adapter = spotAdapter
        binding.rvSpots.layoutManager = LinearLayoutManager(this)

        loadAllSpots()


        binding.btnSpotListClose.setOnClickListener {
            finish()
        }


        spotAdapter.setOnItemClickListener(object : SpotAdapter.OnItemClickListener {
            override fun onItemClick(id: Int) {
                val intent = Intent(this@SpotListActivity, SpotDetailActivity::class.java)
                intent.putExtra("spotId", id)
                startActivity(intent)
            }
        })

    }


    private fun loadAllSpots() {
        CoroutineScope(Dispatchers.IO).launch {
            musicSpotDao.getAllSpots().collect { list ->
                withContext(Dispatchers.Main) {
                    spotAdapter.spots = list
                    spotAdapter.notifyDataSetChanged() //
                }
            }
        }
    }
}