package ddwu.com.mobile.notitest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ddwu.com.mobile.notitest.databinding.ActivityAlertBinding

class AlertActivity : AppCompatActivity() {

    lateinit var alertBinding: ActivityAlertBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        alertBinding = ActivityAlertBinding.inflate(layoutInflater)
        setContentView(alertBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        alertBinding.btnAlertClose.setOnClickListener {
            finish()
        }

    }
}