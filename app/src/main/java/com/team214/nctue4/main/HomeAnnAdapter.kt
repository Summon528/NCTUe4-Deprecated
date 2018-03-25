package com.team214.nctue4.main

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.model.AnnItem
import com.team214.nctue4.utility.E3Type
import kotlinx.android.synthetic.main.item_home_announcement.view.*
import java.text.SimpleDateFormat

class HomeAnnAdapter(private val dataSet: List<AnnItem>, private val fromHome: Boolean,
                     private val itemClickListener: (AnnItem) -> Unit) :
        RecyclerView.Adapter<HomeAnnAdapter.ViewHolder>() {

    class ViewHolder(val view: View, private val fromHome: Boolean,
                     private val itemClickListener: (AnnItem) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bind(ann: AnnItem) {
            view.announcement_name_in_image.text = ann.courseName.first().toString()
            view.announcement_name.text = ann.courseName
            view.announcement_caption.text = ann.caption
            val sdf = SimpleDateFormat("yyyy/MM/dd")
            view.announcement_beginDate.text = sdf.format(ann.beginDate)
            view.setOnClickListener {
                itemClickListener(ann)
            }
            if (!fromHome) {
                view.e3_image.setImageResource(
                        if (ann.e3Type == E3Type.NEW) R.drawable.ic_new_e3
                        else R.drawable.ic_old_e3
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(if (fromHome) R.layout.item_home_announcement_compact else
                    R.layout.item_home_announcement, parent, false)
        return ViewHolder(view, fromHome, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}