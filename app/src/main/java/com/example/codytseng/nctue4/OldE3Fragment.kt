package com.example.codytseng.nctue4

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.codytseng.nctue4.utility.OldE3Connect
import kotlinx.android.synthetic.main.old_e3_fragment.*


/**
 * Created by CodyTseng on 3/12/2018.
 */


class OldE3Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.old_e3_fragment, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val service = OldE3Connect()
        service.getLoginTicket(context) { response ->
            old_e3_textview.text = response.toString()
        }
    }
}