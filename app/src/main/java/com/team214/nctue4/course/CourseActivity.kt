package com.team214.nctue4.course

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.firebase.analytics.FirebaseAnalytics
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.OldE3Connect
import com.team214.nctue4.main.HomeAnnFragment
import com.team214.nctue4.utility.BottomNavigationViewHelper
import kotlinx.android.synthetic.main.activity_course.*

class CourseActivity : AppCompatActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
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
                mFirebaseAnalytics!!.setCurrentScreen(this, "CourseAnnFragment", CourseAnnFragment::class.java.simpleName)
                CourseAnnFragment()
            }
            R.id.course_nav_doc -> {
                mFirebaseAnalytics!!.setCurrentScreen(this, "CourseDocListFragment", CourseDocListFragment::class.java.simpleName)
                CourseDocListFragment()
            }
            R.id.course_nav_assignment -> {
                mFirebaseAnalytics!!.setCurrentScreen(this, "AssignFragment", AssignFragment::class.java.simpleName)
                AssignFragment()
            }
            R.id.course_nav_score -> {
                mFirebaseAnalytics!!.setCurrentScreen(this, "ScoreFragment", ScoreFragment::class.java.simpleName)
                ScoreFragment()
            }
            R.id.course_nav_members -> {
                mFirebaseAnalytics!!.setCurrentScreen(this, "MembersFragment", MembersFragment::class.java.simpleName)
                MembersFragment()
            }
            else -> {
                mFirebaseAnalytics!!.setCurrentScreen(this, "CourseAnnFragment", CourseAnnFragment::class.java.simpleName)
                CourseAnnFragment()
            }
        }
        fragment.arguments = intent.extras
        supportFragmentManager.beginTransaction().replace(R.id.course_container, fragment).commit()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setContentView(R.layout.activity_course)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        BottomNavigationViewHelper.disableShiftMode(course_bottom_nav)

        val bundle = intent.extras
        oldE3Service = bundle.getParcelable("oldE3Service")
        newE3Service = bundle.getParcelable("newE3Service")

        if (savedInstanceState?.getInt("currentFragment") == null)
            switchFragment(-1)

        course_bottom_nav?.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

}
