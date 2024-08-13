package com.example.motorq

import android.os.Bundle
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AssignDriverActivity : AppCompatActivity() {

    private lateinit var driverListView: ListView
    private lateinit var driverAdapter: DriverAdapter
    private var vehicleId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_driver)

        vehicleId = intent.getLongExtra("VEHICLE_ID", 0)

        driverListView = findViewById(R.id.driverListView)
        loadDrivers()

        driverListView.setOnItemClickListener { _, _, position, _ ->
            val selectedDriver = driverAdapter.getItem(position) as Driver
            assignDriverToVehicle(selectedDriver.id)
        }
    }

    private fun loadDrivers() {
        val drivers = listOf(
            Driver(22,"John Doe", "john@example.com", "1234567890", "City A", "08:00 - 16:00"),
            Driver(44,"Jane Doe", "jane@example.com", "0987654321", "City B", "10:00 - 18:00")
        )
        driverAdapter = DriverAdapter(this, drivers)
        driverListView.adapter = driverAdapter
    }

    private fun assignDriverToVehicle(driverId: Long) {
        val client = OkHttpClient()
        val jsonObject = JSONObject().apply {
            put("driver_id", driverId)
            put("vehicle_id", vehicleId)
        }
        val body = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/assignments")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AssignDriverActivity, "Failed to assign driver", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@AssignDriverActivity, "Driver assigned successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@AssignDriverActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
