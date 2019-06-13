package com.selastiansokolowski.healthcarewatch

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.selastiansokolowski.healthcarewatch.ui.HomeFragment
import com.selastiansokolowski.healthcarewatch.ui.WatchDataFragment
import com.selastiansokolowski.healthcarewatch.ui.SettingsFragment
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : DaggerAppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        bottomNavigationView = bottom_navigation_view
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationViewListener)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    HomeFragment()).commit()
        }
    }

    var bottomNavigationViewListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(p0: MenuItem): Boolean {
            val selectedFragment: Fragment? = when (p0.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_data -> WatchDataFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> null
            }

            selectedFragment?.let {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, it).commit()

                return true
            }

            return false
        }
    }

}
