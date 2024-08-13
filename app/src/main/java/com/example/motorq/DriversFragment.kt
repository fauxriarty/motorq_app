package com.example.motorq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import okhttp3.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class DriversFragment : Fragment() {

    private lateinit var driverListView: ListView
    private lateinit var searchView: SearchView
    private lateinit var driverAdapter: DriverAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_drivers, container, false)

        val addDriverFAB: FloatingActionButton = view.findViewById(R.id.addDriverFAB)
        driverListView = view.findViewById(R.id.driverListView)
        searchView = view.findViewById(R.id.searchView)

        val backButton: Button = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(activity, OnboardingActivity::class.java)
            startActivity(intent)
        }

        addDriverFAB.setOnClickListener {
            val intent = Intent(activity, CreateDriverActivity::class.java)
            startActivity(intent)
        }

        loadDrivers()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                driverAdapter.filter.filter(newText)
                return true
            }
        })

        return view
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
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val drivers = parseDriversFromJson(jsonResponse)
                    activity?.runOnUiThread {
                        if (drivers.isEmpty()) {
                            Toast.makeText(requireContext(), "No drivers found", Toast.LENGTH_SHORT).show()
                        } else {
                            driverAdapter = DriverAdapter(requireContext(), drivers)
                            driverListView.adapter = driverAdapter
                        }
                    }
                } else {
                    Log.e("DriversFragment", "Failed to load drivers")
                }
            }
        })
    }

    private fun parseDriversFromJson(jsonResponse: String?): List<Driver> {
        val drivers = mutableListOf<Driver>()
        try {
            if (jsonResponse != null) {
                Log.d("DriversFragment", "Received JSON: $jsonResponse")
                val jsonObject = JSONObject(jsonResponse)
                if (!jsonObject.isNull("data")) {
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val driverObject = jsonArray.getJSONObject(i)
                        val driver = Driver(
                            driverObject.getLong("id"),
                            driverObject.getString("name"),
                            driverObject.getString("email"),
                            driverObject.getString("phone"),
                            driverObject.getString("location"),
                            driverObject.getString("work_hours")
                        )
                        drivers.add(driver)
                    }
                } else {
                    Log.e("DriversFragment", "Data field is null")
                }
            }
        } catch (e: JSONException) {
            Log.e("DriversFragment", "Error parsing JSON", e)
        }
        return drivers
    }

}


