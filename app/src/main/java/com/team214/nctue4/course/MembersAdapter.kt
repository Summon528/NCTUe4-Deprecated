package com.team214.nctue4.course

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.model.MemberItem
import com.team214.nctue4.utility.MemberType
import kotlinx.android.synthetic.main.item_member.view.*

class MembersAdapter(val context: Context, private val dataSet: ArrayList<MemberItem>,
                     private val itemClickListener: (MemberItem) -> Unit) :
        RecyclerView.Adapter<MembersAdapter.ViewHolder>() {

    class ViewHolder(val context: Context, val view: View, private val itemClickListener: (MemberItem) -> Unit) :
            RecyclerView.ViewHolder(view) {
        fun bind(member: MemberItem) {
            view.member_name.text = member.name
            view.member_department.text = member.department
            view.member_email.text = if (member.email == "") context.getString(R.string.no_email) else member.email
            view.course_member_thumb.setColorFilter(
                    when (member.type) {
                        MemberType.TEA -> ContextCompat.getColor(context, R.color.md_orange_700)
                        MemberType.TA -> ContextCompat.getColor(context, R.color.md_green_700)
                        else -> ContextCompat.getColor(context, R.color.md_blue_700)
                    }
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MembersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_member, parent, false)
        return ViewHolder(context, view, itemClickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])

    }

    override fun getItemCount() = dataSet.size
}