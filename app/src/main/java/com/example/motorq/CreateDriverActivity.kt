package com.example.motorq

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*

class CreateDriverActivity : AppCompatActivity() {

    private lateinit var fromTimeText: TextView
    private lateinit var toTimeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_driver)

        val driverNameInput: EditText = findViewById(R.id.driverNameInput)
        val driverEmailInput: EditText = findViewById(R.id.driverEmailInput)
        val driverPhoneInput: EditText = findViewById(R.id.driverPhoneInput)
        val driverLocationInput: EditText = findViewById(R.id.driverLocationInput)
        val createDriverButton: Button = findViewById(R.id.createDriverButton)
        fromTimeText = findViewById(R.id.fromTimeText)
        toTimeText = findViewById(R.id.toTimeText)

        fromTimeText.setOnClickListener { showTimePickerDialog(true) }
        toTimeText.setOnClickListener { showTimePickerDialog(false) }

        createDriverButton.setOnClickListener {
            val name = driverNameInput.text.toString()
            val email = driverEmailInput.text.toString()
            val phone = driverPhoneInput.text.toString()
            val location = driverLocationInput.text.toString()
            val workHours = "${fromTimeText.text} - ${toTimeText.text}"

            if (name.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && fromTimeText.text.isNotEmpty() && toTimeText.text.isNotEmpty()) {
                createDriver(name, email, phone, location, workHours)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimePickerDialog(isFromTime: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            if (isFromTime) {
                fromTimeText.text = time
            } else {
                toTimeText.text = time
            }
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun createDriver(name: String, email: String, phone: String, location: String, workHours: String) {
        val client = OkHttpClient()

        val jsonObject = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("phone", phone)
            put("location", location)
            put("work_hours", workHours)
        }

        val body = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/drivers")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CreateDriverActivity, "Failed to create driver", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@CreateDriverActivity, "Driver created successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@CreateDriverActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
