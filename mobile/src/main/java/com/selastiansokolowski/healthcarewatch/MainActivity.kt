package com.selastiansokolowski.healthcarewatch

import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.selastiansokolowski.healthcarewatch.db.entity.HealthCareEvent
import com.selastiansokolowski.healthcarewatch.service.MessageReceiverService
import com.selastiansokolowski.healthcarewatch.ui.HistoryDataFragment
import com.selastiansokolowski.healthcarewatch.ui.HomeFragment
import com.selastiansokolowski.healthcarewatch.ui.LiveDataFragment
import com.selastiansokolowski.healthcarewatch.ui.SettingsFragment
import com.selastiansokolowski.healthcarewatch.util.SingleEvent
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : DaggerAppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    val healthCareEventSelected: MutableLiveData<SingleEvent<HealthCareEvent>> = MutableLiveData()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        bottomNavigationView = bottom_navigation_view
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationViewListener)

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }

        startService(Intent(this, MessageReceiverService::class.java))
    }

    private var bottomNavigationViewListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(p0: MenuItem): Boolean {
            val selectedFragment: Fragment? = when (p0.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_data -> LiveDataFragment()
                R.id.nav_history -> HistoryDataFragment()
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

    private fun setBottomNavigationSelectedItem(fragment: Fragment) {
        bottomNavigationView.selectedItemId = when (fragment.javaClass) {
            HomeFragment::class.java -> R.id.nav_home
            LiveDataFragment::class.java -> R.id.nav_data
            HistoryDataFragment::class.java -> R.id.nav_history
            SettingsFragment::class.java -> R.id.nav_settings
            else -> R.id.nav_home
        }
    }

    fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                fragment).commit()
        setBottomNavigationSelectedItem(fragment)
    }

}
