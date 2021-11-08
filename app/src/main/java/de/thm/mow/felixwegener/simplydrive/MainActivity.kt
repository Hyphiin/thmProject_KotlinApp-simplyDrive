package de.thm.mow.felixwegener.simplydrive

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*

class MainActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var latHisAdapter: LatHisAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar : MaterialToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val drawerLayout :  DrawerLayout = findViewById(R.id.drawerLayout)
        val navView : NavigationView =  findViewById(R.id.nav_view)
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.homeFragment,R.id.historyFragment,R.id.settingsFragment)
            .setOpenableLayout(drawerLayout)
            .build()
        val navController: NavController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)


        /*toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            when(it.itemId){
                R.id.homeFragment -> Toast.makeText(applicationContext, "Clicked Home", Toast.LENGTH_SHORT).show()
                R.id.historyFragment  -> Toast.makeText(applicationContext, "Clicked History", Toast.LENGTH_SHORT).show()
                R.id.settingsFragment -> Toast.makeText(applicationContext, "Clicked Settings", Toast.LENGTH_SHORT).show()
                R.id.nav_login -> Toast.makeText(applicationContext, "Clicked Login", Toast.LENGTH_SHORT).show()
                R.id.nav_share -> Toast.makeText(applicationContext, "Clicked Share", Toast.LENGTH_SHORT).show()
                R.id.nav_rate_us -> Toast.makeText(applicationContext, "Clicked Rate us", Toast.LENGTH_SHORT).show()
            }

            true
        }

        latHisAdapter = LatHisAdapter(mutableListOf())

        rvLatestHistory.adapter = latHisAdapter
        rvLatestHistory.layoutManager = LinearLayoutManager(this)

        bShowHistory.setOnClickListener{
            val date = "23.08.2021"
            val time = "18:55"
            val route = "Wetzlar Bahnhof - Frankfurt Hbf"

            val newRoute = Route(date, time, route)
            latHisAdapter.addHistory(newRoute)
        }*/
    }

    override fun onSupportNavigateUp(): Boolean {

        val navController: NavController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.homeFragment,R.id.historyFragment,R.id.settingsFragment)
            .setOpenableLayout(drawerLayout)
            .build()

        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }


    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }*/

}