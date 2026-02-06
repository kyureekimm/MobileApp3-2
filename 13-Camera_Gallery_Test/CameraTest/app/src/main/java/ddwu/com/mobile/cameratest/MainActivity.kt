package ddwu.com.mobile.cameratest

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import ddwu.com.mobile.cameratest.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    val TAG="MainActivity"
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

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

        binding.btnTakePic.setOnClickListener {
            dispatchTakePictureIntent()
        }

        binding.btnPickPhoto.setOnClickListener {
            openGallery()
        }

        binding.btnDeletePic.setOnClickListener {
            removeCurrentImage()
        }
    }


    /*카메라 Intent를 호출하여 사진 촬영을 시작*/
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(takePictureIntent.resolveActivity(packageManager) != null) {
            //파일 만들어짐.
            val photoFile: File? = try {
                val file = FileUtil.createNewFile(this)
                currentPhotoPath = file.absolutePath
                file
            } catch (e: IOException) {
                Log.e(TAG, "이미지 파일 생성 오류", e)
                null
            }

            photoFile?.also {
                //uri로 가져옴.
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${application.packageName}.fileprovider", //권한정보
                    it
                )
                currentPhotoUri = photoURI //uri 형태로 권한 증서 ><!!

                //MediaStore.EXTRA_OUTPUT 를 권한정보, 파일정보 갖고있는 photoURI에다가..
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePictureLauncher.launch(takePictureIntent)
            }

        }else {
            Log.e(TAG, "카메라 앱 미확인")
        }



    }


    /*카메라 호출 및 결과 처리를 위한 ActivityResultLauncher*/
    private val takePictureLauncher =registerForActivityResult(

        ActivityResultContracts.StartActivityForResult() ) { result ->

        if(result.resultCode ==RESULT_OK) {
            displayImage(currentPhotoUri!!)
        }else {
            Toast.makeText(this, "사진촬영 취소", Toast.LENGTH_SHORT).show()
            removeCurrentImage()
        }

    }

    /*uri에 저장된 이미지를 ImageView에 표시*/
    private fun displayImage(uri: Uri?) {
        try{
            uri?.also {
                Glide.with(this).load(uri).into(binding.imageView)
            }
        } catch (e: Exception) {
            Log.d(TAG, "이미지 확인 불가: $uri", e)
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
                    .into(binding.imageView)
                currentPhotoUri = null
                currentPhotoPath = null
            }
        }catch (e: IOException){
            Log.e(TAG, "이미지 삭제 오류", e)
        }
    }


    /*갤러리 앱 호출 및 결과 처리를 위한 ActivityResultLauncher*/
//    private val pickImageLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == RESULT_OK) {
//            result.data?.data?.let { uri ->     // 선택된 이미지의 URI
//                currentPhotoUri = uri
//                displayImage(currentPhotoUri) // 선택된 이미지 확인 (ImageView에 표시)
//                currentPhotoPath = FileUtil.saveFileToExtStorage(this, currentPhotoUri)
//            }
//        } else {
//            Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_SHORT).show()
//        }
//    }


    /*갤러리 앱 실행*/
    val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()) {uri: Uri? ->
        if(uri != null) {
            Glide.with(this).load(uri).into(binding.imageView)

            currentPhotoUri = uri
            currentPhotoPath =
                FileUtil.saveFileToExtStorage(this, currentPhotoUri)

            Toast.makeText(this, "사진을 가져왔습니다.", Toast.LENGTH_SHORT).show()
        }else {
            Toast.makeText(this, "선택이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }



    /*갤러리 앱 실행*/
    private fun openGallery() {
       // val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch("image/*")

    }


    /*카메라 촬영 권한 요청 처리*/
    private fun checkCameraPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    //checkCameraPermission안되면 하는거?
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