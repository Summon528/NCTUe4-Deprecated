package com.team214.nctue4.main

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3WebConnect
import com.team214.nctue4.connect.NewE3WebInterface

class NewE3Fragment : Fragment() {

    private lateinit var newE3Service: NewE3WebConnect
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        activity!!.setTitle(R.string.new_e3)
        return inflater.inflate(R.layout.fragment_blank, container,false)
    }

    private fun getData(){
        val newE3Service = (activity as MainActivity).newE3Service
        newE3Service.getAnn { status, response ->
            when (status) {
                NewE3WebInterface.Status.SUCCESS -> {

                    Log.d("ya", response!!.size.toString())
                    for (i in response){
                        Log.d("i", i.toString())
                    }
                    Log.d("ya", response.toString())
                }
                else -> {
                    Log.d("error", response.toString())
                }
            }
        }
    }
}