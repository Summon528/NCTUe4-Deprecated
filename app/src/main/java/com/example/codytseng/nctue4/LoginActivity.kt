package com.example.codytseng.nctue4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.codytseng.nctue4.utility.OldE3Connect
import com.example.codytseng.nctue4.utility.OldE3Interface
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        student_id.setText(prefs.getString("studentId", ""))
        student_password.setText(prefs.getString("studentPassword", ""))
        login_button.setOnClickListener {
            login_progressbar.visibility = View.VISIBLE;
            val studentId = student_id.text.toString()
            val studentPassword = student_password.text.toString()
            val service = OldE3Connect()
            service.loginSetup(studentId, studentPassword, this) { status, response ->
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        val prefsEditor = prefs.edit()
                        prefsEditor.putString("studentId", studentId)
                        prefsEditor.putString("studentPassword", studentPassword)
                        prefsEditor.putString("studentName", response!!.first)
                        prefsEditor.putString("studentEmail", response!!.second)
                        prefsEditor.commit()
                        login_progressbar.visibility = View.GONE;
                        val intent = Intent()
                        intent.putExtra("studentName", response!!.first);
                        intent.putExtra("studentEmail", response!!.second);
                        setResult(Activity.RESULT_OK, intent);
                        finish()
                    }
                    OldE3Interface.Status.WRONG_CREDENTIALS -> {
                        login_error_textview.text = getString(R.string.login_id_or_password_error)
                        login_error_textview.visibility = View.VISIBLE
                        login_progressbar.visibility = View.GONE;
                    }
                    else -> {
                        login_error_textview.text = getString(R.string.generic_error)
                        login_error_textview.visibility = View.VISIBLE
                        login_progressbar.visibility = View.GONE;
                    }
                }
            }
        }
    }
}
