package ddwu.com.mobile.naverretrofittest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import ddwu.com.mobile.naverretrofittest.data.Book
import ddwu.com.mobile.naverretrofittest.data.Root
import ddwu.com.mobile.naverretrofittest.databinding.ActivityMainBinding
import ddwu.com.mobile.naverretrofittest.network.IBookSearchService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var adapter : BookAdapter
    lateinit var bookSearchService: IBookSearchService

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

        val retrofit : Retrofit = Retrofit.Builder()
            .baseUrl( resources.getString(R.string.naver_book_search_url) )
            .addConverterFactory( GsonConverterFactory.create() )
            .build()

        bookSearchService = retrofit.create( IBookSearchService::class.java )


        binding.btnSearch.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val root : Root = bookSearchService.getBookList(
                    binding.etKeyword.text.toString(),
                    resources.getString(R.string.client_id),
                    resources.getString(R.string.client_secret),
                )

                adapter.items = root.items
                adapter.notifyDataSetChanged()
            }
        }

        adapter.setOnItemClickListener(object : BookAdapter.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                val book = adapter.items?.get(position) //as? Book
//                if (book != null) {
//                    Glide.with(this@MainActivity)
//                        .load(book.image)
//                        .placeholder(R.drawable.ic_launcher_foreground)
//                        .into(binding.imageView)

                    val intent = Intent(this@MainActivity, DetailActivity::class.java)
                    intent.putExtra("book", book)
                    startActivity(intent)

                Log.d("MainActivity", "인텐트 전송됨!")
                }

        })
    }
}