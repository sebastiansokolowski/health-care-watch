package com.sebastiansokolowski.healthguard.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class LocationModel(private val context: Context) : LocationCallback() {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fun requestLocation(timeout: Int, callback: LocationCallback) {
        var disposable: Disposable? = null
        val gmsLocationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                disposable?.dispose()
                callback.onLocationResult(locationResult?.locations?.first())
            }

            override fun onLocationAvailability(p0: LocationAvailability?) {
            }
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationResult(null)
            return
        }
        disposable = startTimeout(timeout, gmsLocationCallback, callback)
        fusedLocationClient.requestLocationUpdates(LocationRequest.create(), gmsLocationCallback, Looper.myLooper())
    }

    private fun startTimeout(timeout: Int, gmsLocationCallback: com.google.android.gms.location.LocationCallback, callback: LocationCallback): Disposable? {
        return Observable.timer(timeout.toLong(), TimeUnit.SECONDS)
                .subscribe {
                    fusedLocationClient.removeLocationUpdates(gmsLocationCallback)
                    callback.onLocationResult(null)
                }
    }

    interface LocationCallback {
        fun onLocationResult(location: Location?)
    }
}