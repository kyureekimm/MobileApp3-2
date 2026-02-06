package ddwu.com.mobile.alarmtest

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ddwu.com.mobile.alarmtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    val alarmManager by lazy {
        getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

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


        createNotificationChannel()

        binding.btnOneShot.setOnClickListener {
            // 일회성 알람 구현
            checkNotificationPermission()//알람 권한 확인

            val intent = Intent(this, MyBReceiver::class.java).apply {
                action = "ddwu.com.mobile.alarmtest.ACTION_ALARM"
                putExtra("ALARM_MESSAGE", "일어나세용~")
            }

            val pendingIntent : PendingIntent
                = PendingIntent.getBroadcast(this, 0,
                intent, PendingIntent.FLAG_IMMUTABLE )

            //ms니까 곱하기1000해야 5초
            alarmManager?.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 5 * 1000,
                pendingIntent)

        }



        binding.btnRepeat.setOnClickListener {
            // 반복 알람 구현
//            val pendingIntent : PendingIntent =

//            alarmManager.setInexactRepeating(
//                AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
//                AlarmManager.INTERVAL_HOUR,
//                pendingIntent
//            )

        }

        binding.btnStopAlarm.setOnClickListener {
            // 반복 알람 중지 구현
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "알람 채널"
            val descriptionText = "알람을 위한 채널입니다."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("ALARM_CHANNEL", name, importance).apply {
                description = descriptionText
            }

            // 채널을 시스템에 등록
            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    /*알림 권한 확인*/
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 권한이 없는 경우 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    /*권한 요청 결과 확인*/
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(applicationContext, "사용권한 승인, 버튼 다시 클릭!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "권한 필요", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }




}