package ddwu.com.mobile.naverretrofittest


import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import com.bumptech.glide.request.transition.Transition
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import ddwu.com.mobile.naverretrofittest.data.Book
import ddwu.com.mobile.naverretrofittest.databinding.ActivityDetailBinding
import java.io.File
import java.io.FileOutputStream

class DetailActivity : AppCompatActivity() {
    lateinit var detailBinding: ActivityDetailBinding

    private var savedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        detailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(detailBinding.root)


        val item = intent.getSerializableExtra("book") as? Book

        if (item != null) {
            Glide.with(this)
                .load(item.image)
                .into(detailBinding.imageView2)


            detailBinding.btnImageSave.setOnClickListener {
                Glide.with(this)
                    .asBitmap()
                    .load(item.image)
                    .into(object : CustomTarget<Bitmap>(350, 350) {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {

                            val fileName = "${System.currentTimeMillis()}.jpg"
                            val imageFile = File(filesDir, fileName)

                            val fos = FileOutputStream(imageFile)
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            fos.close()

                            savedFile = imageFile

                            Log.d("DetailActivity", "File saved: ${imageFile.absolutePath}")
                            Toast.makeText(applicationContext, "저장 완료: ${imageFile.name}", Toast.LENGTH_SHORT).show()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.d("DetailActivity", "Image load cleared!")
                        }
                    })
            }
        } else {
            Toast.makeText(this, "데이터를 불러오는 데 실패함", Toast.LENGTH_SHORT).show()
            finish()
        }

        detailBinding.btnClose.setOnClickListener {

            if (savedFile != null && savedFile!!.exists()) {
                savedFile!!.delete()
                Log.d("DetailActivity", "삭제된 파일: ${savedFile!!.name}")
            }

            finish()
        }
    }
}