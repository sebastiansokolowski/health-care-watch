package com.sebastiansokolowski.healthcarewatch

import android.arch.lifecycle.MutableLiveData
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.MenuItem
import com.sebastiansokolowski.healthcarewatch.client.WearableDataClient
import com.sebastiansokolowski.healthcarewatch.db.entity.HealthCareEventEntity
import com.sebastiansokolowski.healthcarewatch.service.MessageReceiverService
import com.sebastiansokolowski.healthcarewatch.ui.*
import com.sebastiansokolowski.healthcarewatch.util.SingleEvent
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    val healthCareEventEntitySelected: MutableLiveData<SingleEvent<HealthCareEventEntity>> = MutableLiveData()

    @Inject
    lateinit var wearableDataClient: WearableDataClient

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

    override fun onResume() {
        super.onResume()
        wearableDataClient.sendLiveData(true)
    }

    override fun onStop() {
        super.onStop()
        wearableDataClient.sendLiveData(false)
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

    private fun setBottomNavigationSelectedItem(fragment: Fragment) {
        bottomNavigationView.selectedItemId = when (fragment.javaClass) {
            HomeFragment::class.java -> R.id.nav_home
            LiveDataFragment::class.java -> R.id.nav_data
            HistoryDataFragment::class.java -> R.id.nav_history
            SettingsFragment::class.java -> R.id.nav_settings
            AdvancedSettingsFragment::class.java -> return
            else -> R.id.nav_home
        }
    }

    fun showFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val fragmentTransaction = supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()

        setBottomNavigationSelectedItem(fragment)
    }

    fun showDialog(dialogFragment: DialogFragment) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack(null)
        dialogFragment.show(fragmentTransaction, "dialog")
    }
}
