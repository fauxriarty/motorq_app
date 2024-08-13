package com.example.motorq

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

data class Vehicle(val id: Long, val make: String, val model: String, val licensePlate: String)

class VehicleAdapter(private val context: Context, private val vehicles: List<Vehicle>) : BaseAdapter() {

    override fun getCount(): Int = vehicles.size

    override fun getItem(position: Int): Any = vehicles[position]

    override fun getItemId(position: Int): Long = vehicles[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_vehicle, parent, false)
        val vehicle = vehicles[position]

        view.findViewById<TextView>(R.id.vehicleMakeModel).text = "${vehicle.make} ${vehicle.model}"
        view.findViewById<TextView>(R.id.vehicleLicensePlate).text = vehicle.licensePlate

        return view
    }
}
