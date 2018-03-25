package com.team214.nctue4

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.team214.nctue4.main.MainActivity
import com.team214.nctue4.utility.NewE3Connect
import com.team214.nctue4.utility.NewE3Interface
import com.team214.nctue4.utility.OldE3Connect
import com.team214.nctue4.utility.OldE3Interface
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File


class LoginActivity : AppCompatActivity() {
    private lateinit var oldE3Service: OldE3Connect
    private lateinit var newE3Service: NewE3Connect
    private var oldE3Success = false
    private var newE3Success = false

    override fun onStop() {
        super.onStop()
        if (::oldE3Service.isInitialized) oldE3Service.cancelPendingRequests()
        if (::newE3Service.isInitialized) newE3Service.cancelPendingRequests()
    }

    private fun loginSuccess() {
        if (oldE3Success and newE3Success) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            if (Looper.myLooper() == null) Looper.prepare()
            Toast.makeText(this@LoginActivity, getString(R.string.login_success), Toast.LENGTH_SHORT).show()
            Looper.loop()
        }
    }

    private var isWrongCredentials = false
    private fun showWrongCredentials() {
        if (isWrongCredentials) return
        isWrongCredentials = true
        runOnUiThread {
            login_error_text_view.text = getString(R.string.login_id_or_password_error)
            login_error_text_view?.visibility = View.VISIBLE
            login_progressbar?.visibility = View.GONE
            student_id.isEnabled = true
            student_password.isEnabled = true
            student_portal_password.isEnabled = true
        }
    }

    private var isServiceError = false
    private fun showServiceError() {
        if (isServiceError) return
        isServiceError = true
        runOnUiThread {
            login_error_text_view.text = getString(R.string.generic_error)
            login_error_text_view?.visibility = View.VISIBLE
            login_progressbar?.visibility = View.GONE
            student_id.isEnabled = true
            student_password.isEnabled = true
            student_portal_password.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (intent.getBooleanExtra("logout", false)) {
            Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            val prefsEditor = prefs.edit()
            prefsEditor.remove("studentId")
            prefsEditor.remove("studentPassword")
            prefsEditor.remove("studentPortalPassword")
            prefsEditor.remove("oldE3Bookmarked")
            prefsEditor.apply()
            val path = this.getExternalFilesDir(null)
            val dir = File(path, "Download")
            dir.deleteRecursively()
        } else {
            val studentId = prefs.getString("studentId", "")
            val studentPassword = prefs.getString("studentPassword", "")
            val studentPortalPassword = prefs.getString("studentPortalPassword", "")
            if (studentId != "" && studentPassword != "" && studentPortalPassword != "") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        setContentView(R.layout.activity_login)
        login_button?.setOnClickListener {
            isServiceError = false
            isWrongCredentials = false
            student_id.isEnabled = false
            student_password.isEnabled = false
            student_portal_password.isEnabled = false
            login_progressbar?.visibility = View.VISIBLE
            val studentId = student_id.text.toString()
            val studentPassword = student_password.text.toString()
            val studentPortalPassword = student_portal_password.text.toString()
            val oldService = OldE3Connect(studentId, studentPassword)
            oldService.getLoginTicket { status, response ->
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        val prefsEditor = prefs.edit()
                        prefsEditor.putString("studentId", studentId)
                        prefsEditor.putString("studentPassword", studentPassword)
                        prefsEditor.apply()
                        oldE3Success = true
                        loginSuccess()
                    }
                    OldE3Interface.Status.WRONG_CREDENTIALS -> {
                        showWrongCredentials()
                    }
                    else -> {
                        showServiceError()
                    }
                }
            }
            newE3Service = NewE3Connect(studentId, studentPortalPassword)
            newE3Service.getCookie { status, response ->
                when (status) {
                    NewE3Interface.Status.SUCCESS -> {
                        val prefsEditor = prefs.edit()
                        prefsEditor.putString("studentId", studentId)
                        prefsEditor.putString("studentPortalPassword", studentPortalPassword)
                        prefsEditor.apply()
                        newE3Success = true
                        loginSuccess()
                    }
                    NewE3Interface.Status.WRONG_CREDENTIALS -> {
                        showWrongCredentials()
                    }
                    else -> {
                        showServiceError()
                    }
                }
            }
        }
    }
}
