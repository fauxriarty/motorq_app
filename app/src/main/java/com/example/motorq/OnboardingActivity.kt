package com.example.motorq

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding)

        val managerButton: Button = findViewById(R.id.manager)
        val driverButton: Button = findViewById(R.id.driver)
//
//        val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
//        val animationDrawable = rootLayout.background as AnimationDrawable
//        animationDrawable.setEnterFadeDuration(2000)
//        animationDrawable.setExitFadeDuration(4000)
//        animationDrawable.start()

        managerButton.setOnClickListener {
            val intent = Intent(this, ManagerMainActivity::class.java)
            startActivity(intent)
        }

        driverButton.setOnClickListener {
            val intent = Intent(this, DriverLoginActivity::class.java)
            startActivity(intent)
        }
    }
}
