package com.example.motorq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class DriverDashboardActivity : AppCompatActivity() {

    private var driverId: Long = 0
    private lateinit var lastRideText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        val welcomeText: TextView = findViewById(R.id.welcomeText)
        lastRideText = findViewById(R.id.lastRideText)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        val driverName = intent.getStringExtra("DRIVER_NAME")
        driverId = intent.getLongExtra("DRIVER_ID", 0)
        Log.d("DriverDashboardActivity", "Driver ID in Dashboard: $driverId")
        welcomeText.text = "Welcome, $driverName!"

        fetchLastRide()

        bottomNavigationView.selectedItemId = R.id.navigation_profile
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> true
                R.id.navigation_assignments -> {
                    val assignmentsIntent = Intent(this, DriverAssignmentsActivity::class.java)
                    assignmentsIntent.putExtra("DRIVER_ID", driverId)
                    startActivity(assignmentsIntent)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchLastRide() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/drivers/$driverId/lastRide")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    lastRideText.text = "Failed to fetch last ride details"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                    val lastRide = jsonResponse.optString("lastRide", "No rides yet")
                    runOnUiThread {
                        lastRideText.text = "Your last ride was in the $lastRide"
                    }
                } else {
                    runOnUiThread {
                        lastRideText.text = "Failed to fetch last ride details"
                    }
                }
            }
        })
    }
}


