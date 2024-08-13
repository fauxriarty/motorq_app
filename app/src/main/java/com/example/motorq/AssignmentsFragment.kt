package com.example.motorq

import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class AssignmentsFragment : Fragment() {

    private lateinit var startTimeText: EditText
    private lateinit var endTimeText: EditText
    private lateinit var driverSpinner: Spinner
    private lateinit var vehicleSpinner: Spinner
    private lateinit var assignButton: Button
    private lateinit var unassignButton: Button

    private var driverIds = mutableListOf<Long>()
    private var vehicleIds = mutableListOf<Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assignments, container, false)

        driverSpinner = view.findViewById(R.id.driverSpinner)
        vehicleSpinner = view.findViewById(R.id.vehicleSpinner)
        startTimeText = view.findViewById(R.id.startTimeText)
        endTimeText = view.findViewById(R.id.endTimeText)
        assignButton = view.findViewById(R.id.assignButton)
        unassignButton = view.findViewById(R.id.unassignButton)

        startTimeText.setOnClickListener { showTimePickerDialog(startTimeText) }
        endTimeText.setOnClickListener { showTimePickerDialog(endTimeText) }

        assignButton.setOnClickListener {
            val driverId = driverIds[driverSpinner.selectedItemPosition]
            val vehicleId = vehicleIds[vehicleSpinner.selectedItemPosition]
            val startTime = startTimeText.text.toString()
            val endTime = endTimeText.text.toString()

            if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                assignDriverToVehicle(driverId, vehicleId, startTime, endTime)
            } else {
                Toast.makeText(context, "Please fill in both start and end times", Toast.LENGTH_SHORT).show()
            }
        }

        unassignButton.setOnClickListener {
            val driverId = driverIds[driverSpinner.selectedItemPosition]
            val vehicleId = vehicleIds[vehicleSpinner.selectedItemPosition]
            val startTime = startTimeText.text.toString()
            val endTime = endTimeText.text.toString()

            if (startTime.isNotEmpty() && endTime.isNotEmpty()) {
                unassignDriverFromVehicle(driverId, vehicleId, startTime, endTime)
            } else {
                Toast.makeText(context, "Please fill in both start and end times", Toast.LENGTH_SHORT).show()
            }
        }

        loadDrivers()
        loadVehicles()

        return view
    }

    private fun showTimePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            editText.setText(time)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    private fun loadDrivers() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/drivers")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to load drivers", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    parseDriversFromJson(jsonResponse)
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to load drivers", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun loadVehicles() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/vehicles")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to load vehicles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    parseVehiclesFromJson(jsonResponse)
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to load vehicles", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun parseDriversFromJson(jsonResponse: String?) {
        try {
            if (jsonResponse != null) {
                val jsonObject = JSONObject(jsonResponse)
                val driverNames = mutableListOf<String>()
                if (!jsonObject.isNull("data")) {
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val driverObject = jsonArray.getJSONObject(i)
                        driverIds.add(driverObject.getLong("id"))
                        driverNames.add(driverObject.getString("name"))
                    }
                    activity?.runOnUiThread {
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, driverNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        driverSpinner.adapter = adapter
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun parseVehiclesFromJson(jsonResponse: String?) {
        try {
            if (jsonResponse != null) {
                val jsonObject = JSONObject(jsonResponse)
                val vehicleNames = mutableListOf<String>()
                if (!jsonObject.isNull("data")) {
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val vehicleObject = jsonArray.getJSONObject(i)
                        vehicleIds.add(vehicleObject.getLong("id"))
                        vehicleNames.add("${vehicleObject.getString("make")} ${vehicleObject.getString("model")}")
                    }
                    activity?.runOnUiThread {
                        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, vehicleNames)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        vehicleSpinner.adapter = adapter
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun assignDriverToVehicle(driverId: Long, vehicleId: Long, startTime: String, endTime: String) {
        val client = OkHttpClient()

        val jsonObject = JSONObject().apply {
            put("driver_id", driverId)
            put("vehicle_id", vehicleId)
            put("start_time", startTime)
            put("end_time", endTime)
        }

        val body = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/assignments")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to assign driver", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Driver assigned successfully", Toast.LENGTH_SHORT).show()
                        loadDrivers()
                    } else {
                        try {
                            val jsonResponse = JSONObject(response.body?.string() ?: "")
                            val errorMessage = jsonResponse.optString("error", "Error: ${response.message}")
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        } catch (e: JSONException) {
                            Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }

    private fun unassignDriverFromVehicle(driverId: Long, vehicleId: Long, startTime: String, endTime: String) {
        val client = OkHttpClient()

        val jsonObject = JSONObject().apply {
            put("driver_id", driverId)
            put("vehicle_id", vehicleId)
        }

        val body = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/assignments/unassign")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Failed to unassign driver", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                activity?.runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Driver unassigned successfully", Toast.LENGTH_SHORT).show()
                        loadDrivers()
                    } else {
                        try {
                            val jsonResponse = JSONObject(response.body?.string() ?: "")
                            val errorMessage = jsonResponse.optString("error", "Error: ${response.message}")
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        } catch (e: JSONException) {
                            Toast.makeText(context, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }


}
