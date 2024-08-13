package com.example.motorq

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Filter
import android.widget.Filterable

data class Driver(val id: Long, val name: String, val email: String, val phone: String, val location: String, val workHours: String)

class DriverAdapter(private val context: Context, private var driverList: List<Driver>) : BaseAdapter(), Filterable {

    private var filteredDriverList: List<Driver> = driverList

    override fun getCount(): Int {
        return filteredDriverList.size
    }

    override fun getItem(position: Int): Any {
        return filteredDriverList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_driver, parent, false)

        val driver = filteredDriverList[position]
        view.findViewById<TextView>(R.id.driverName).text = driver.name
        view.findViewById<TextView>(R.id.driverEmail).text = driver.email
        view.findViewById<TextView>(R.id.driverPhone).text = driver.phone
        view.findViewById<TextView>(R.id.driverLocation).text = driver.location
        view.findViewById<TextView>(R.id.driverWorkHours).text = driver.workHours

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.toLowerCase()?.trim()
                val filteredResults = if (TextUtils.isEmpty(query)) {
                    driverList
                } else {
                    driverList.filter {
                        it.name.toLowerCase().contains(query!!) || it.phone.contains(query)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredResults
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredDriverList = results?.values as List<Driver>
                notifyDataSetChanged()
            }
        }
    }
}
