package com.team214.nctue4.course

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.team214.nctue4.BlankFragment
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.utility.BottomNavigationViewHelper
import kotlinx.android.synthetic.main.activity_course.*

class CourseActivity : AppCompatActivity() {
    var oldE3Service: OldE3Connect? = null
    var newE3Service: NewE3Connect? = null
    private var currentFragment = -1

    override fun onStop() {
        super.onStop()
        oldE3Service?.cancelPendingRequests()
        newE3Service?.cancelPendingRequests()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("currentFragment", currentFragment)
    }

    private val mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item ->
                switchFragment(item.itemId)
                return@OnNavigationItemSelectedListener true
            }


    private fun switchFragment(itemId: Int) {
        currentFragment = itemId
        val fragment = when (itemId) {
            R.id.course_nav_ann -> {
                CourseAnnFragment()
            }
            R.id.course_nav_doc -> {
                CourseDocListFragment()
            }
            R.id.course_nav_assignment -> {
                BlankFragment()
            }
            R.id.course_nav_score -> {
                BlankFragment()
            }
            R.id.course_nav_members -> {
                MembersFragment()
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
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        BottomNavigationViewHelper.disableShiftMode(course_bottom_nav)

        val bundle = intent.extras
        oldE3Service = bundle.getParcelable("oldE3Service")
        newE3Service = bundle.getParcelable("newE3Service")

        switchFragment(
                if (savedInstanceState?.getInt("currentFragment") != null)
                    savedInstanceState.getInt("currentFragment")
                else -1)


        course_bottom_nav?.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

}
