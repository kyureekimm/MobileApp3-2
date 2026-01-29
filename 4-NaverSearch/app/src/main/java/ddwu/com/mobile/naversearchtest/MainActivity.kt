package ddwu.com.mobile.naversearchtest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import ddwu.com.mobile.naversearchtest.data.network.Book
import ddwu.com.mobile.naversearchtest.data.network.BookParser
import ddwu.com.mobile.naversearchtest.data.network.NetworkService
import ddwu.com.mobile.naversearchtest.databinding.ActivityMainBinding
import ddwu.com.mobile.naversearchtest.ui.BookAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"
    lateinit var binding: ActivityMainBinding
    lateinit var adapter : BookAdapter

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

        adapter = BookAdapter()
        binding.rvBooks.adapter = adapter
        binding.rvBooks.layoutManager = LinearLayoutManager(this)

        binding.btnSearch.setOnClickListener {
            val address = resources.getString(R.string.book_search_url) /*strings.xml 의 url 주소 확인*/
            val query = binding.etKeyword.text.toString() // XML의 EditText ID 'etKeyword' 사용
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val finalUrl = address + encodedQuery

//            val params = HashMap<String, String>()
//            /*HashMap에 URL에 결합할 파라메터와 값 지정*/
//            params["X-Naver-Client-Id"] = resources.getString(R.string.client_id)
//            params["X-Naver-Client-Secret"] = resources.getString(R.string.client_secret)




            CoroutineScope(Dispatchers.Main).launch {
                val xmlResult = withContext(Dispatchers.IO) {
                    // 4. NetworkService를 생성하고, 검색어가 포함된 finalUrl로 요청!
                    NetworkService(this@MainActivity).sendRequest("GET", finalUrl, null)
                }

                val books = xmlResult?.let {
                    withContext(Dispatchers.IO) {
                        val parser = BookParser() // 파서 객체 생성
                        parser.parse(it)          // 네트워크 결과를 파서에 전달
                    }
                }

                // 3. 파싱 결과(books)가 null이 아닐 경우에만 RecyclerView에 데이터 설정
                if (books != null) {
                    adapter.books = books
                    adapter.notifyDataSetChanged()
                }

            }
        }
    }
}