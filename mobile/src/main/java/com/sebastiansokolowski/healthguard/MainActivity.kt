package com.sebastiansokolowski.healthguard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sebastiansokolowski.healthguard.db.entity.HealthEventEntity
import com.sebastiansokolowski.healthguard.model.MeasurementModel
import com.sebastiansokolowski.healthguard.service.WearableService
import com.sebastiansokolowski.healthguard.ui.*
import com.sebastiansokolowski.healthguard.util.SingleEvent
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    val healthEventEntitySelected: MutableLiveData<SingleEvent<HealthEventEntity>> = MutableLiveData()

    @Inject
    lateinit var measurementModel: MeasurementModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        bottomNavigationView = bottom_navigation_view
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationViewListener)

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }

        startService(Intent(this, WearableService::class.java))
    }

    override fun onResume() {
        super.onResume()
        measurementModel.changeLiveDataState(true)
    }

    override fun onStop() {
        super.onStop()
        measurementModel.changeLiveDataState(false)
    }

    private var bottomNavigationViewListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(p0: MenuItem): Boolean {
            val selectedFragment: androidx.fragment.app.Fragment? = when (p0.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_data -> LiveDataFragment()
                R.id.nav_history -> HistoryDataFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> null
            }

            selectedFragment?.let {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                }
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, it).commit()
                return true
            }

            return false
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun setBottomNavigationSelectedItem(fragment: androidx.fragment.app.Fragment) {
        bottomNavigationView.selectedItemId = when (fragment.javaClass) {
            HomeFragment::class.java -> R.id.nav_home
            LiveDataFragment::class.java -> R.id.nav_data
            HistoryDataFragment::class.java -> R.id.nav_history
            SettingsFragment::class.java -> R.id.nav_settings
            AdvancedSettingsFragment::class.java -> return
            else -> R.id.nav_home
        }
    }

    fun showFragment(fragment: androidx.fragment.app.Fragment, addToBackStack: Boolean = false) {
        val fragmentTransaction = supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()

        setBottomNavigationSelectedItem(fragment)
    }

    fun showDialog(dialogFragment: androidx.fragment.app.DialogFragment) {
        val fragmentTransaction: androidx.fragment.app.FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack(null)
        dialogFragment.show(fragmentTransaction, "dialog")
    }
}
