package de.thm.mow.felixwegener.simplydrive

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_gps.*
import java.util.jar.Manifest

class GpsActivity2 : AppCompatActivity() {

    private val defaultUpdateInterval = 30
    private val fastUpdateInterval = 5
    private val permissionsFineLocation = 99
    private val permissionsCoarseLocation = 88

    private lateinit var locationRequest : LocationRequest

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps2)

        tv_backToHome.setOnClickListener {
            startActivity(Intent(this@GpsActivity2, MainActivity::class.java))
            finish()
        }

        locationRequest = LocationRequest()

        locationRequest.interval = (((1000 * defaultUpdateInterval).toLong()))
        locationRequest.fastestInterval = ((1000 * fastUpdateInterval).toLong())

        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        //Functions
        sw_gps.setOnClickListener {
            if (sw_gps.isChecked) {
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                tv_sensor.text = "Using GPS sensors"
            } else {
                locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                tv_sensor.text = "Using Towers + WIFI"
            }
        }

        updateGPS()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
           permissionsFineLocation -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
               updateGPS()
           } else {
               Toast.makeText(
                   this@GpsActivity2,
                   "Diese Funktion benötigt eine Zustimmung um zu funktionieren",
                   Toast.LENGTH_SHORT
               ).show()
           }
        }
    }

    private fun updateGPS() {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location : Location? ->
                if (location != null) {
                    updateUIValues(location)
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), permissionsFineLocation)
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), permissionsFineLocation)
            }
        }
    }

    private fun updateUIValues(location: Location) {
        tv_lat.text = location.latitude.toString()
        tv_lon.text = location.longitude.toString()
        tv_accuracy.text = location.accuracy.toString()

        if (location.hasAltitude()) {
            tv_altitude.text = location.altitude.toString()
        }else{
            tv_altitude.text = "Not availible"
        }
        if (location.hasSpeed()) {
            tv_speed.text = location.speed.toString()
        }else{
            tv_speed.text = "Not availible"
        }

    }
}