package com.example.codytseng.nctue4.course

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.example.codytseng.nctue4.BlankFragment
import com.example.codytseng.nctue4.R
import com.example.codytseng.nctue4.utility.BottomNavigationViewHelper
import com.example.codytseng.nctue4.utility.OldE3Connect
import com.example.codytseng.nctue4.utility.OldE3Interface
import kotlinx.android.synthetic.main.activity_course.*

class CourseActivity : AppCompatActivity() {
    val service = OldE3Connect()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        switchFragment(item.itemId)
        return@OnNavigationItemSelectedListener true
    }


    private fun switchFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.course_nav_ann -> {
                CourseAnnFragment()
            }
            R.id.course_nav_doc -> {
                CourseDocFragment()
            }
            R.id.course_nav_assignment -> {
                BlankFragment()
            }
            R.id.course_nav_score -> {
                BlankFragment()
            }
            R.id.course_nav_other -> {
                BlankFragment()
            }
            else -> {
                CourseAnnFragment()
            }
        }
        fragment.arguments = intent.extras
        supportFragmentManager.beginTransaction().replace(R.id.course_container, fragment).commit()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val studentId = prefs.getString("studentId", "")
        val studentPassword = prefs.getString("studentPassword", "")
        BottomNavigationViewHelper.disableShiftMode( course_bottom_nav)

        service.getLoginTicket(studentId, studentPassword) { status, _ ->
            when (status) {
                OldE3Interface.Status.SUCCESS -> {
                    switchFragment(-1)
                }
            }
        }

        course_bottom_nav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

}