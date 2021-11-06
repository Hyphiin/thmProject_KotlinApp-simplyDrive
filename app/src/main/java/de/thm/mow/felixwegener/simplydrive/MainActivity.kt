package de.thm.mow.felixwegener.simplydrive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var latHisAdapter: LatHisAdapter

    lateinit var dateInput: EditText
    lateinit var timeInput: EditText
    lateinit var routeInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            when (it.itemId) {
                R.id.nav_home -> Toast.makeText(
                    applicationContext,
                    "Clicked Home",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.nav_history -> Toast.makeText(
                    applicationContext,
                    "Clicked History",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.nav_setting -> Toast.makeText(
                    applicationContext,
                    "Clicked Settings",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.nav_login -> Toast.makeText(
                    applicationContext,
                    "Clicked Login",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.nav_share -> Toast.makeText(
                    applicationContext,
                    "Clicked Share",
                    Toast.LENGTH_SHORT
                ).show()
                R.id.nav_rate_us -> Toast.makeText(
                    applicationContext,
                    "Clicked Rate us",
                    Toast.LENGTH_SHORT
                ).show()
            }

            true
        }

        latHisAdapter = LatHisAdapter(mutableListOf())

        rvLatestHistory.adapter = latHisAdapter
        rvLatestHistory.layoutManager = LinearLayoutManager(this)

        bShowHistory.setOnClickListener {
            addDBEntry()
        }

        clearHistory.setOnClickListener {
            clearDB()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun addDBEntry() {

        dateInput = findViewById<View>(R.id.DateInput) as EditText
        timeInput = findViewById<View>(R.id.TimeInput) as EditText
        routeInput = findViewById<View>(R.id.RouteInput) as EditText

        val date = dateInput.text.toString()
        val time = timeInput.text.toString()
        val route = routeInput.text.toString()

        val newRoute = Route(date, time, route)
        latHisAdapter.addHistory(newRoute)
    }

    private fun clearDB() {
        latHisAdapter.clearDB()
    }

}