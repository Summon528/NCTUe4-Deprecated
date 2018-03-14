package com.example.codytseng.nctue4

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_course.*

class CourseActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        switchFragment(item.itemId)
        return@OnNavigationItemSelectedListener true
    }

    fun switchFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.navigation_home -> {
                CourseAnnFragment()
            }
            R.id.navigation_dashboard -> {
                BlankFragment()
            }
            R.id.navigation_notifications -> {
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
        switchFragment(-1)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}
