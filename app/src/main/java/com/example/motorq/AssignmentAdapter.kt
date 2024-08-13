package com.example.motorq

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

data class Assignment(val id: Long, val title: String, val details: String)

class AssignmentAdapter(
    private val context: Context,
    private val assignments: List<Assignment>,
    private val onAccept: (Long) -> Unit,
    private val onReject: (Long) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = assignments.size

    override fun getItem(position: Int): Assignment = assignments[position]

    override fun getItemId(position: Int): Long = assignments[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false)

        val assignment = getItem(position)

        val title: TextView = view.findViewById(R.id.assignmentTitle)
        val details: TextView = view.findViewById(R.id.assignmentDetails)
        val btnAccept: Button = view.findViewById(R.id.btnAccept)
        val btnReject: Button = view.findViewById(R.id.btnReject)

        title.text = assignment.title
        details.text = assignment.details

        btnAccept.setOnClickListener { onAccept(assignment.id) }
        btnReject.setOnClickListener { onReject(assignment.id) }

        return view
    }
}
