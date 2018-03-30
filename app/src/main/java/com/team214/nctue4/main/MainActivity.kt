package com.team214.nctue4.main

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.team214.nctue4.LoginActivity
import com.team214.nctue4.R
import com.team214.nctue4.connect.NewE3Connect
import com.team214.nctue4.connect.NewE3WebConnect
import com.team214.nctue4.connect.OldE3Connect
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var currentFragment = -1
    lateinit var oldE3Service: OldE3Connect
    lateinit var newE3WebService: NewE3WebConnect
    lateinit var newE3Service: NewE3Connect

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt("currentFragment", currentFragment)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Main)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val studentId = prefs.getString("studentId", "")
        val studentPassword = prefs.getString("studentPassword", "")
        val studentPortalPassword = prefs.getString("studentPortalPassword", "")
        val studentEmail = prefs.getString("studentEmail", "")
        val studentName = prefs.getString("studentName", "")
        val newE3Token = prefs.getString("newE3Token", "")
        val newE3UserId = prefs.getString("newE3UserId", "")

        oldE3Service = OldE3Connect(studentId, studentPassword)
        newE3WebService = NewE3WebConnect(studentId, studentPortalPassword, "")
        newE3Service = NewE3Connect(studentId, studentPortalPassword, newE3UserId, newE3Token)
        currentFragment = if (savedInstanceState?.getInt("currentFragment") != null)
            savedInstanceState.getInt("currentFragment") else -1
        switchFragment(currentFragment)

        nav_view.getHeaderView(0).findViewById<TextView>(R.id.student_name).text = studentName
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.student_email).text = studentEmail
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else if (currentFragment != R.id.nav_home) {
            switchFragment(R.id.nav_home)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun switchFragment(id: Int) {
        if (id != -1)
            nav_view.setCheckedItem(id)
        else
            nav_view.setCheckedItem(R.id.nav_home)
        val fragment = when (id) {
            R.id.nav_home -> {
                currentFragment = id
                HomeFragment()
            }
            R.id.nav_ann -> {
                currentFragment = id
                HomeAnnFragment()
            }
            R.id.nav_bookmarked -> {
                currentFragment = id
                BookmarkedFragment()
            }
            R.id.nav_download -> {
                currentFragment = id
                DownloadFragment()
            }
            R.id.nav_old_e3 -> {
                currentFragment = id
                OldE3Fragment()
            }
            R.id.nav_new_e3 -> {
                currentFragment = id
                NewE3Fragment()
            }
            R.id.nav_log_out -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.logout)
                        .setMessage(R.string.logout_confirm)
                        .setPositiveButton(R.string.positive) { _, _ ->
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.putExtra("logout", true)
                            startActivity(intent)
                            finish()
                        }.setNegativeButton(R.string.cancel) { dialog, _ ->
                            dialog.cancel()
                        }.show()
                null
            }
            R.id.nav_about -> {
                LicenseDialog().show(supportFragmentManager, "TAG")
                null
            }
            else -> {
                currentFragment = R.id.nav_home
                HomeFragment()
            }
        }
        if (fragment != null) {
            supportFragmentManager.beginTransaction().replace(R.id.main_container, fragment, "main_fragment").commit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        switchFragment(item.itemId)
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


}
