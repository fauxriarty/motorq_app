package com.example.motorq

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class CreateVehicleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_vehicle)

        val vehicleMakeInput: EditText = findViewById(R.id.vehicleMakeInput)
        val vehicleModelInput: EditText = findViewById(R.id.vehicleModelInput)
        val vehicleLicensePlateInput: EditText = findViewById(R.id.vehicleLicensePlateInput)
        val createVehicleButton: Button = findViewById(R.id.createVehicleButton)

        createVehicleButton.setOnClickListener {
            val make = vehicleMakeInput.text.toString()
            val model = vehicleModelInput.text.toString()
            val licensePlate = vehicleLicensePlateInput.text.toString()

            if (make.isNotEmpty() && model.isNotEmpty() && licensePlate.isNotEmpty()) {
                createVehicle(make, model, licensePlate)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createVehicle(make: String, model: String, licensePlate: String) {
        val client = OkHttpClient()

        val jsonObject = JSONObject().apply {
            put("make", make)
            put("model", model)
            put("license_plate", licensePlate)
            put("assigned_driver_id", JSONObject.NULL)
        }

        val body = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/vehicles")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CreateVehicleActivity, "Failed to create vehicle", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CreateVehicleActivity, "Vehicle created successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity after creation
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreateVehicleActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
