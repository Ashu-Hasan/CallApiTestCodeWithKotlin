package com.ashu.callapitestcode.UILayers

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.ashu.callapitestcode.R

class SplashActivity : AppCompatActivity() {

    private lateinit var logo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logo = findViewById(R.id.logo)

        // 🔥 Animation (scale + fade)
        logo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(1200)
            .setStartDelay(200)
            .withEndAction {

                // ⏭ Move to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }
}