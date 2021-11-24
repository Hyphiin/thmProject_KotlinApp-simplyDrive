package de.thm.mow.felixwegener.simplydrive

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_gps.*
import kotlinx.android.synthetic.main.activity_show_saved_locations_list.*

class ShowSavedLocationsListActivity : AppCompatActivity() {

    private lateinit var savedLocations: MutableList<Location>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_saved_locations_list)

        val myApplication = applicationContext as MyApplication
        savedLocations = myApplication.getMyLocations() as MutableList<Location>

        lv_waypoints.adapter = ArrayAdapter<Location>(this, android.R.layout.simple_list_item_1, savedLocations)

        tv_backToGps.setOnClickListener {
            startActivity(Intent(this@ShowSavedLocationsListActivity, GpsActivity::class.java))
            finish()
        }
    }
}