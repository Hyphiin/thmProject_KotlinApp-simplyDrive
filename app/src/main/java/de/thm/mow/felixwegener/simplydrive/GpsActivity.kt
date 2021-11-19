package de.thm.mow.felixwegener.simplydrive

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import kotlinx.android.synthetic.main.activity_gps.*


class GpsActivity : AppCompatActivity() {

    private val defaultUpdateInterval = 30
    private val fastUpdateInterval = 5

    lateinit var locationRequest: LocationRequest

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_gps)

        Toast.makeText(
            this@GpsActivity,
            "Gps",
            Toast.LENGTH_SHORT
        ).show()

        tv_backToHome.setOnClickListener {
            startActivity(Intent(this@GpsActivity, MainActivity::class.java))
            finish()
        }

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
    }

}