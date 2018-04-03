package com.team214.nctue4.course

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.model.AssignItem
import kotlinx.android.synthetic.main.item_assign.view.*
import java.text.SimpleDateFormat
import java.util.*

class AssignAdapter(private val dataSet: ArrayList<AssignItem>,
                    private val itemClickListener: (AssignItem) -> Unit) :
        RecyclerView.Adapter<AssignAdapter.ViewHolder>() {

    class ViewHolder(val view: View, private val itemClickListener: (AssignItem) -> Unit) :
            RecyclerView.ViewHolder(view) {
        private val df = SimpleDateFormat("yyyy/M/d", Locale.TAIWAN)
        fun bind(assign: AssignItem) {
            view.assign_name.text = assign.name
            view.assign_start.text = df.format(assign.startDate)
            view.assign_end.text = df.format(assign.endDate)
            view.assign_item.setOnClickListener { itemClickListener(assign) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): AssignAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_assign, parent, false)
        return ViewHolder(view, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}