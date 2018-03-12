package com.example.codytseng.nctue4

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.old_e3_fragment.*

/**
 * Created by CodyTseng on 3/12/2018.
 */


class OldE3Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.old_e3_fragment, null);
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//         Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this.context)
        val url = "http://e3.nctu.edu.tw/mService/service.asmx/Login"
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val studentId = prefs.getString("student_id", "")
        val studentPassword = prefs.getString("student_password", "")
        // Request a string response from the provided URL.
        val stringRequest = object : StringRequest(Request.Method.POST, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    old_e3_textview.text = response
                },
                Response.ErrorListener { old_e3_textview.text = "That didn't work!" }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params.put("account", studentId)
                params.put("password", studentPassword)
                return params
            }
        }
//        // Add the request to the RequestQueue.
        queue.add(stringRequest)

    }
}