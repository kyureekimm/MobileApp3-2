package ddwu.com.mobile.miniproject3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//import com.bumptech.glide.Glide
import ddwu.com.mobile.miniproject3.data.Memo
import ddwu.com.mobile.miniproject3.databinding.ActivityMemoDetailBinding
import java.io.File
import kotlin.getValue

class MemoDetailActivity : AppCompatActivity() {

    val binding by lazy { ActivityMemoDetailBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        val memo =


        binding.imageViewPicked.setOnClickListener {

        }

        binding.btnUpdate.setOnClickListener {
            val memoUpdate = Memo( , ) //가져온 아이디 넣어야됨.
            finish()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }
}