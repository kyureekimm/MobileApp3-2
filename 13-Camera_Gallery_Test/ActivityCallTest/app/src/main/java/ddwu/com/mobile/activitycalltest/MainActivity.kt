package ddwu.com.mobile.activitycalltest

import android.Manifest
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ddwu.com.mobile.activitycalltest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val TAG = "CALL_TEST"
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 기존 방법
        binding.btnOld.setOnClickListener {
            val intent = Intent(this, SubActivity::class.java)
            intent.putExtra("message", "startActivityForResult!")
            startActivityForResult(intent, 100)
        }

        // 신규 방법
        binding.btnNew.setOnClickListener {
            val intent = Intent(this, SubActivity::class.java)
            intent.putExtra("message", "ActivityResultLauncher!")
            callSubActivityRequest.launch(intent)
        }


        // 1. SubActivity 실행
        binding.btnSub.setOnClickListener {
            val intent = Intent(this, SubActivity::class.java)
            intent.putExtra("message", "Call Activity!")
            subActivityLauncher.launch(intent)
        }

        // 2. 갤러리 열기 ("image/*" 로 모든 이미지 필터링)
        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")   // image/jpeg, image/png 등
        }

        // 3. 권한 요청 실행
        binding.btnPermission.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.CAMERA)
//            if (checkSelfPermission(Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
//                permissionLauncher.launch(Manifest.permission.CAMERA)
//            }
        }

    }

    /*기존 방법 결과 확인*/
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val text = data?.getStringExtra("result_text")
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    val callSubActivityRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(), {
            result ->
            if (result.resultCode == RESULT_OK) {
                val text = result.data?.getStringExtra("result_text")
                Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
            }
        }
    )


    // SubActivity 결과 수신용 Launcher  사용 Contract: StartActivityForResult()
    private val subActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {  // 결과 처리
                Log.d(TAG, "${result.data?.getStringExtra("result_text")}")
            }
        }

    //갤러리 이미지 선택용 Launcher  사용 Contract: GetContent() (입력: MIME type String, 결과: Uri?)
    private val galleryLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // uri 에 해당하는 이미지 사용
                Log.d(TAG, "선택 이미지 URI: ${uri}")
            }
        }

    // 카메라 권한 요청용 Launcher  사용 Contract: RequestPermission() (입력: Permission String, 결과: Boolean)
    private val permissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            // 권한 승인 처리
            Log.d(TAG, "권한 요청 결과: ${isGranted}")
        }

}