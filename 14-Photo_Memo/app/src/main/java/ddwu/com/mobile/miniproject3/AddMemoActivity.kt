package ddwu.com.mobile.miniproject3

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import ddwu.com.mobile.miniproject3.data.Memo
import ddwu.com.mobile.miniproject3.data.MemoDatabase
import ddwu.com.mobile.miniproject3.databinding.ActivityAddMemoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class AddMemoActivity : AppCompatActivity() {

    val TAG = "AddMemoActivity"
    val binding by lazy { ActivityAddMemoBinding.inflate(layoutInflater) }

    val memoDao by lazy {
        MemoDatabase.getInstance(this).getMemoDao()
    }

    private var currentPhotoUri: Uri? = null    // 촬영 또는 선택한 사진의 URI를 저장할 변수
    private var currentPhotoPath: String? = null  // 현재 사진 파일의 경로를 저장할 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkCameraPermission()  // 카메라 촬영 권한 확인

        binding.imageViewPicked.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("사진 촬영/선택")
                setMessage("카메라 또는 갤러리 사용")
                setPositiveButton("카메라") { _, _ -> openCamera() }
                setNegativeButton("갤러리") { _, _ -> openGallery() }
                setNeutralButton("취소", null)
                show()
           } //1학기 내용.
        }

        binding.btnSave.setOnClickListener {
            val newMemo = Memo(R.id.etTitle.text.toString(), R.id.etContents.text.toString(), "" )
            CoroutineScope(Dispatchers.IO).launch{
                memoDao.insertMemo(newMemo)
            }
            finish()
        }

        binding.btnCancel.setOnClickListener {
            removeCurrentImage()
            finish()
        }
    }



    /*카메라 Intent를 호출하여 사진 촬영을 시작*/
    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE) //이 요청을 보내면 안드로이드가 설치된 카메라 앱 중 하나를 띄워줍니다.

        //resolveActivity: 현재 폰에 내 요청(ACTION_IMAGE_CAPTURE)을 처리해줄 수 있는 앱(카메라 앱)이 있는지 검사합니다
        //앱이 있을때만 실행.
        if(takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File?
            = try {
                val file = FileUtil.createNewFile(this) //1.파일생성
                currentPhotoPath = file.absolutePath            // 2. 경로저장
                file                                            // 3. 변수에 담기
            } catch (e: IOException) {
                Log.e(TAG, "이미지 파일 생서 오류, e")
                null
            }

            photoFile?.also {
                val photoURI : Uri  = FileProvider.getUriForFile(
                    this,
                    "${application.packageName}.fileprovider",
                    it
                )
                currentPhotoUri = photoURI

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
            }
        }
    }



    /*카메라 호출 및 결과 처리를 위한 ActivityResultLauncher*/
    //registerForActivityResult(Contract, Callback) : Launcher 사용
    private val takePictureLauncher = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult() ) {
        result ->
        if(result.resultCode == RESULT_OK) {
            displayImage(currentPhotoUri!!)
        } else {
            Toast.makeText(this, "사진촬영 취소 ", Toast.LENGTH_SHORT).show()
            removeCurrentImage()
        }

    }


    )


    /*갤러리 앱 실행*/
    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    /*갤러리 앱 실행*/
    val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()) {uri : Uri? ->
        if(uri != null) {
            Glide.with(this).load(uri).into(binding.imageViewPicked)

            currentPhotoUri = uri
            currentPhotoPath =
                FileUtil.saveFileToExtStorage(this, currentPhotoUri)

            Toast.makeText(this, "사진을 가져왔습니다.", Toast.LENGTH_SHORT).show()
        }else {
            Toast.makeText(this, "선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }



    /*uri에 저장된 이미지를 ImageView에 표시*/
    private fun displayImage(uri: Uri?) {
        try{
            uri?.also {
                Glide.with(this)
                    .load(uri)
                    .into(binding.imageViewPicked)
            }

        }catch (e: Exception) {
            Log.d(TAG, "이미지 확인 불가 : &uri", e)
        }
    }


    /*currentPhotoPath 의 이미지 삭제 및 초기화*/
    private fun removeCurrentImage() {
        //파일 취소하고 사진뜬것도 내려!!!
        try {
            val isRemoved = FileUtil.deleteFile(currentPhotoPath)
            if(isRemoved) {
                Glide.with(this)
                    .load(android.R.drawable.ic_menu_camera)
                    .into(binding.imageViewPicked)
                currentPhotoUri = null
                currentPhotoPath = null
            }
        } catch (e: IOException) {
            Log.e(TAG, "이미지 삭제 오류", e)
        }
    }


    /*카메라 촬영 권한 요청 처리*/
    private fun checkCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    /*권한 요청을 위한 ActivityResultLauncher*/
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "카메라 권한 승인", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "카메라 권한이 필요", Toast.LENGTH_SHORT).show()
        }
    }

}