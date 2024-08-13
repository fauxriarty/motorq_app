package com.example.motorq

import android.os.Bundle
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DriverAssignmentsActivity : AppCompatActivity() {

    private lateinit var assignmentsListView: ListView
    private lateinit var searchView: SearchView
    private var driverName: String = ""
    private var driverId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_assignments)

        assignmentsListView = findViewById(R.id.assignmentsListView)
        searchView = findViewById(R.id.searchView)

        driverName = intent.getStringExtra("DRIVER_NAME") ?: ""
        Log.d("DriverAssignmentsActivity", "Driver Name: $driverName")

        fetchDriverId(driverName)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.navigation_assignments
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_profile -> {
                    finish()
                    true
                }
                R.id.navigation_assignments -> true
                else -> false
            }
        }

        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun fetchDriverId(driverName: String) {
        val client = OkHttpClient()
        val url = "https://motorq-backend.onrender.com/drivers/search?name=$driverName"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DriverAssignmentsActivity, "Failed to fetch driver ID", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)
                    val driversArray = jsonResponse.getJSONArray("data")

                    if (driversArray.length() > 0) {
                        val driver = driversArray.getJSONObject(0)
                        driverId = driver.getLong("id")
                        Log.d("DriverAssignmentsActivity", "Driver ID: $driverId")

                        fetchAssignments(driverId)
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@DriverAssignmentsActivity, "Driver not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DriverAssignmentsActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchAssignments(driverId: Long) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/drivers/$driverId/assignments")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DriverAssignmentsActivity, "Failed to fetch assignments", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                    val assignmentsArray = jsonResponse.optJSONArray("data") ?: JSONArray()
                    runOnUiThread {
                        displayAssignments(assignmentsArray)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DriverAssignmentsActivity, "Failed to fetch assignments", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun displayAssignments(assignments: JSONArray) {
        val assignmentList = mutableListOf<Assignment>()

        for (i in 0 until assignments.length()) {
            val assignment = assignments.getJSONObject(i)
            val startTime = formatTime(assignment.getString("start_time"))
            val endTime = formatTime(assignment.getString("end_time"))
            val status = assignment.getString("status")
            assignmentList.add(
                Assignment(
                    assignment.getLong("id"),
                    "Assignment from $startTime to $endTime",
                    "Status: $status"
                )
            )
        }

        val adapter = AssignmentAdapter(this, assignmentList, ::acceptAssignment, ::rejectAssignment)
        assignmentsListView.adapter = adapter
    }

    private fun formatTime(timeString: String): String {
        val possibleDateFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX"
        )

        var date: Date? = null
        for (format in possibleDateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                date = sdf.parse(timeString)
                if (date != null) break
            } catch (e: Exception) {
                // Ignore and try the next format
            }
        }

        return if (date != null) {
            val outputFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            "Unknown Date"
        }
    }

    private fun rejectAssignment(assignmentId: Long) {
        handleAssignmentResponse(assignmentId, "reject")
    }

    private fun acceptAssignment(assignmentId: Long) {
        handleAssignmentResponse(assignmentId, "accept")
    }

    private fun handleAssignmentResponse(assignmentId: Long, action: String) {
        val client = OkHttpClient()

        val jsonObject = JSONObject().apply {
            put("driver_id", driverId)
            put("assignment_id", assignmentId)
        }

        val body = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val url = when (action) {
            "accept" -> "https://motorq-backend.onrender.com/assignments/accept"
            "reject" -> "https://motorq-backend.onrender.com/assignments/reject"
            else -> return
        }

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@DriverAssignmentsActivity, "Failed to $action assignment", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@DriverAssignmentsActivity, "Assignment $action-ed successfully", Toast.LENGTH_SHORT).show()
                        fetchAssignments(driverId)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@DriverAssignmentsActivity, "Failed to $action assignment", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
