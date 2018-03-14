package com.example.codytseng.nctue4

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.codytseng.nctue4.utility.OldE3Connect
import com.example.codytseng.nctue4.utility.OldE3Interface
import kotlinx.android.synthetic.main.activity_ann.*
import kotlinx.android.synthetic.main.fragment_course_ann.*

/**
 * Created by s094392 on 3/14/18.
 */
class AnnActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ann)
        var intent =  intent.extras
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val studentId = prefs.getString("studentId", "")
        val studentPassword = prefs.getString("studentPassword", "")
        val annId = intent.getString("annId")
        Log.d("arguments", annId)
        val service = OldE3Connect()
        service.getLoginTicket(studentId, studentPassword) { status, response ->
            when (status) {
                OldE3Interface.Status.SUCCESS -> {
                    service.getAnnouncementDetail(annId) { status, response ->
                        when (status) {
                            OldE3Interface.Status.SUCCESS -> {
                                if (ann_text_view != null) {
                                    ann_text_view.text = response.toString()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}