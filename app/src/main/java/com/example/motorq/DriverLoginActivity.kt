package com.example.motorq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class DriverLoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        val loginButton: Button = findViewById(R.id.loginButton)
        val driverNameInput: EditText = findViewById(R.id.driverNameInput)

        loginButton.setOnClickListener {
            val driverName = driverNameInput.text.toString()

            if (driverName.isNotEmpty()) {
                checkDriverExists(driverName)
            } else {
                driverNameInput.error = "Please enter your name"
            }
        }
    }

    private fun checkDriverExists(driverName: String) {
        val client = OkHttpClient()
        val url = "https://motorq-backend.onrender.com/drivers/search?name=$driverName"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DriverLoginActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val driversArray = jsonResponse.getJSONArray("data")

                    if (driversArray.length() > 0) {
                        val driver = driversArray.getJSONObject(0)
                        val driverId = driver.getLong("id")

                        runOnUiThread {
                            val intent = Intent(this@DriverLoginActivity, DriverDashboardActivity::class.java)
                            intent.putExtra("DRIVER_NAME", driverName)
                            intent.putExtra("DRIVER_ID", driverId)
                            Log.d("DriverLoginActivity", "Driver ID: $driverId")

                            startActivity(intent)
                            finish()
                        }

                    } else {
                        runOnUiThread {
                            Toast.makeText(this@DriverLoginActivity, "Driver not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DriverLoginActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
