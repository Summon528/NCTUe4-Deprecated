package com.example.codytseng.nctue4

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button.setOnClickListener {
            val prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit()
            val studentId = student_id.text.toString()
            val studentPassword = student_password.text.toString()
            prefsEditor.putString("studentId", studentId)
            prefsEditor.putString("studentPassword", studentPassword)
            Toast.makeText(this, studentPassword, Toast.LENGTH_SHORT)
            prefsEditor.commit()
            finish()
        }
    }
}
