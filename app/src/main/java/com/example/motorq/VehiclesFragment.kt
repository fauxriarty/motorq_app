package com.example.motorq

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class VehiclesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vehicles, container, false)

        val addVehicleButton: FloatingActionButton = view.findViewById(R.id.addVehicleButton)
        val vehicleListView: ListView = view.findViewById(R.id.vehicleListView)

        addVehicleButton.setOnClickListener {
            val intent = Intent(activity, CreateVehicleActivity::class.java)
            startActivity(intent)
        }

        // Load vehicle data into the ListView (this can be done asynchronously)
        loadVehicles(vehicleListView)

        return view
    }

    private fun loadVehicles(listView: ListView) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://motorq-backend.onrender.com/vehicles")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val vehicles = parseVehiclesFromJson(jsonResponse)
                    activity?.runOnUiThread {
                        if (isAdded) {
                            val adapter = VehicleAdapter(requireContext(), vehicles)
                            listView.adapter = adapter
                        }
                    }

                } else {
                    Log.e("VehiclesFragment", "Failed to load vehicles")
                }
            }
        })
    }

    private fun parseVehiclesFromJson(jsonResponse: String?): List<Vehicle> {
        val vehicles = mutableListOf<Vehicle>()
        try {
            if (jsonResponse != null) {
                val jsonObject = JSONObject(jsonResponse)
                if (!jsonObject.isNull("data")) {
                    val jsonArray = jsonObject.getJSONArray("data")
                    for (i in 0 until jsonArray.length()) {
                        val vehicleObject = jsonArray.getJSONObject(i)
                        val vehicle = Vehicle(
                            vehicleObject.getLong("id"),
                            vehicleObject.getString("make"),
                            vehicleObject.getString("model"),
                            vehicleObject.getString("license_plate")
                        )
                        vehicles.add(vehicle)
                    }
                } else {
                    Log.e("VehiclesFragment", "Data field is null")
                }
            }
        } catch (e: JSONException) {
            Log.e("VehiclesFragment", "Error parsing JSON", e)
        }
        return vehicles
    }

}
