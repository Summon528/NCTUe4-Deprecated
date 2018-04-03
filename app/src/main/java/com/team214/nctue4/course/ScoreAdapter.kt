package com.team214.nctue4.course

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.model.ScoreItem
import kotlinx.android.synthetic.main.item_score.view.*

class ScoreAdapter(private val dataSet: ArrayList<ScoreItem>) :
        RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {

    class ViewHolder(val view: View) :
            RecyclerView.ViewHolder(view) {
        fun bind(score: ScoreItem) {
            view.score_name.text = score.name
            view.score_score.text = score.score
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ScoreAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_score, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount() = dataSet.size
}