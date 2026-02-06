package ddwu.com.mobile.alarmtest

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MyBReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 전달받은 intent 에서 ALARM_MESSGE 확인 구현
//        val alarmMessage = intent?.getStringExtra("ALARM_MESSAGE")
//        Log.d("MyBReceiver", "ALARM_MESSAGE: ${alarmMessage}")
        // 1. MainActivity와 동일한 채널 ID 사용


            val alarmMessage = intent?.getStringExtra("ALARM_MESSAGE")
            Log.d("MyBReceiver", "ALARM_MESSAGE: ${alarmMessage}")

        val intent = Intent(context, MainActivity::class.java).apply {
            // 새 태스크로 열거나, 기존 태스크를 맨 위로 가져옴
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent : PendingIntent
            = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE )

            // 2. 알림 빌더 생성
            val builder = NotificationCompat.Builder(context, "ALARM_CHANNEL")
                .setSmallIcon(R.drawable.ic_stat_name) // ◀ 아이콘 리소스 확인!
                .setContentTitle("기상 시간")
                .setContentText("일어나! 티켓팅할 시간이야!") // 메시지 사용
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            // 3. 알림 매니저로 알림 실행
            val notiManager = NotificationManagerCompat.from(context)
            notiManager.notify(100, builder.build())


    }
}

    //실습 : 알람 울릴때 로그말고 노티가 뜨게
