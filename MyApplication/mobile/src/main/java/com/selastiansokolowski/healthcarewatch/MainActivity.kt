package com.selastiansokolowski.healthcarewatch

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.android.gms.wearable.Wearable
import com.selastiansokolowski.healthcarewatch.listener.SensorDataListener
import com.selastiansokolowski.healthcarewatch.ui.DataFragment
import com.selastiansokolowski.healthcarewatch.ui.HomeFragment
import com.selastiansokolowski.healthcarewatch.ui.SettingsFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var sensorDataListener: SensorDataListener

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        sensorDataListener = SensorDataListener()

        bottomNavigationView = bottom_navigation_view
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationViewListener)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                    HomeFragment()).commit()
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(sensorDataListener)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(sensorDataListener)
    }

    var bottomNavigationViewListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(p0: MenuItem): Boolean {
            val selectedFragment: Fragment? = when (p0.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_data -> DataFragment()
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
