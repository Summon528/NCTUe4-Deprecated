package com.team214.nctue4

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3Interface
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.connect.OldE3Interface
import com.team214.nctue4.main.MainActivity
import com.team214.nctue4.model.CourseDBHelper
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
            login_button?.text = getString(R.string.login)
            student_id.isEnabled = true
            student_password.isEnabled = true
            student_portal_password.isEnabled = true
            login_button?.isEnabled = true
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
            login_button?.text = getString(R.string.login)
            student_id.isEnabled = true
            student_password.isEnabled = true
            student_portal_password.isEnabled = true
            login_button?.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        super.onCreate(savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (intent.getBooleanExtra("logout", false)) {
            Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            val prefsEditor = prefs.edit()
            prefsEditor.clear().apply()
            val dbHelper = CourseDBHelper(this)
            dbHelper.delTable()
            val path = this.getExternalFilesDir(null)
            val dir = File(path, "Download")
            dir.deleteRecursively()
        } else {
            val studentId = prefs.getString("studentId", "")
            val studentPassword = prefs.getString("studentPassword", "")
            val studentPortalPassword = prefs.getString("studentPortalPassword", "")
            val versionCode = prefs.getInt("versionCode", -1)
            if (studentId != "" && studentPassword != "" && studentPortalPassword != "" &&
                    versionCode >= 18) {
                prefs.edit().putInt("versionCode", packageManager.getPackageInfo(packageName, 0).versionCode).apply()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        prefs.edit().putInt("versionCode", packageManager.getPackageInfo(packageName, 0).versionCode).apply()
        setContentView(R.layout.activity_login)

        //detect soft keyboard
        login_layout.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = login_layout.rootView.height - login_layout.height
            if (heightDiff > dpToPx(200f)) {
                login_scroll_view.smoothScrollBy(0, heightDiff)
            }
        }

        login_button?.setOnClickListener {
            isServiceError = false
            isWrongCredentials = false
            student_id.isEnabled = false
            student_password.isEnabled = false
            student_portal_password.isEnabled = false
            login_progressbar?.visibility = View.VISIBLE
            login_button?.text = ""
            login_button?.isEnabled = false
            val studentId = student_id.text.toString()
            val studentPassword = student_password.text.toString()
            var studentPortalPassword = student_portal_password.text.toString()
            if (studentPortalPassword == "") studentPortalPassword = studentPassword
            val oldService = OldE3Connect(studentId, studentPassword)
            oldService.getLoginTicket { status, response ->
                when (status) {
                    OldE3Interface.Status.SUCCESS -> {
                        val prefsEditor = prefs.edit()
                        prefsEditor.putString("studentId", studentId)
                        prefsEditor.putString("studentPassword", studentPassword)
                        prefsEditor.putString("studentEmail", response!!.second)
                        prefsEditor.putString("studentName", response.first)
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
            newE3Service.getToken { status, response ->
                when (status) {
                    NewE3Interface.Status.SUCCESS -> {
                        val prefsEditor = prefs.edit()
                        prefsEditor.putString("studentPortalPassword", studentPortalPassword)
                        prefsEditor.putString("newE3Token", response)
                        prefsEditor.apply()
                        newE3Service.getUserId { status2, response2 ->
                            if (status2 == NewE3Interface.Status.SUCCESS) {
                                prefsEditor.putString("newE3UserId", response2).apply()
                                newE3Success = true
                                loginSuccess()
                            } else showServiceError()
                        }
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

    private fun dpToPx(valueInDp: Float): Float {
        val metrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
    }
}
