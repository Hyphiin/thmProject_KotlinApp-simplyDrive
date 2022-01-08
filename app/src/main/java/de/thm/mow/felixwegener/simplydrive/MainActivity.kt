package de.thm.mow.felixwegener.simplydrive

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.thm.mow.felixwegener.simplydrive.databinding.ActivityMainBinding
import de.thm.mow.felixwegener.simplydrive.fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_SHOW_CARD_FRAG
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_START_OR_RESUME_SERVICE
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_STOP_SERVICE
import de.thm.mow.felixwegener.simplydrive.services.TrackingService
import java.io.File


class MainActivity : AppCompatActivity(), ScanFragment.OnDataPass, EditFragment.OnDataPass {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    var storage = Firebase.storage

    //Fragments
    private var startDrive: Boolean = false

    private val homeFragment = HomeFragment()
    private val settingsFragment = SettingsFragment()
    private val historyFragment = HistoryFragment()
    private val editFragment = EditFragment()
    private val scanFragment = ScanFragment()
    private val mapsFragment = MapsFragment()
    private val profileFragment = ProfileFragment()
    private val driveFragment = CardDriveFragment()

    //GPS
    private val defaultUpdateInterval = 5
    private val fastUpdateInterval = 2
    private val permissionsFineLocation = 99

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallBack: LocationCallback

    private lateinit var currentLocation: Location

    //FAB Button(s)
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    private var clicked = false

    private lateinit var currentDriveId: String

    override fun onDataPass(data: String) {
        currentDriveId = data

        // set
        (this.application as MyApplication).setDriveId(currentDriveId)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToTrackingFragmentIfNeeded(intent)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(4).isEnabled = false

        //Fragments
        checkHomeScreen()

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> replaceFragment(homeFragment)
                R.id.nav_setting -> sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
                R.id.nav_history -> replaceFragment(historyFragment)
                R.id.nav_map -> replaceFragment(mapsFragment)
            }
            true
        }

        //FAB Button(s)
        fab_main.setOnClickListener {
            onAddButtonClicked()
            fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_add, this.theme));
        }
        fab_scan.setOnClickListener {
            replaceFragment(scanFragment)
            fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_scanner, this.theme));

        }
        fab_edit.setOnClickListener {
            replaceFragment(editFragment)
            fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_search, this.theme));
        }

        //GPS
        locationRequest = LocationRequest()

        locationRequest.interval = (1000 * defaultUpdateInterval).toLong()
        locationRequest.fastestInterval = (1000 * fastUpdateInterval).toLong()

        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        locationCallBack = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d("Tracked Location:", locationResult.toString());
                if (locationResult != null) {
                    uploadLocation(locationResult)
                }
                locationResult ?: return
                /*for (location in locationResult.locations) {
                    updateUIValues(location)
                }*/
            }
        }
        updateGPS()
        startLocationUpdates()

        //PROFILE
        profilePic.setOnClickListener {
            replaceFragment(profileFragment)
        }

        firebaseAuth = FirebaseAuth.getInstance()

        retrieveUserImage()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun retrieveUserImage() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null) {
            val imageRef = storage.reference.child("images/${firebaseUser.uid}")


            val localFile = File.createTempFile("tempImage", "jpg")
            imageRef.getFile(localFile).addOnSuccessListener {
                Log.d("Found User-Img:", imageRef.path)
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                img__currentUser.setImageBitmap(bitmap)
            }.addOnFailureListener {
                Log.d("Failed finding User-Img", "Loading Fallback Image!")
                val fallbackImage = storage.reference.child("images/maxe.png")
                fallbackImage.getFile(localFile).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                    img__currentUser.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun checkHomeScreen() {
        startDrive = (application as MyApplication).getStartDrive()!!
        if (startDrive === true) {
            replaceFragment(homeFragment)
        } else {
            replaceFragment(driveFragment)
        }
    }


    private fun replaceFragment(fragment: Fragment) {
        fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_add, this.theme));
        if (clicked) {
            onAddButtonClicked()
        }
        if (fragment != null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, fragment, "fragmentTag")
            fragmentTransaction.commit()
        }
        firebaseAuth = FirebaseAuth.getInstance()
        retrieveUserImage()
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fab_scan.visibility = View.VISIBLE
            fab_edit.visibility = View.VISIBLE
        } else {
            fab_scan.visibility = View.INVISIBLE
            fab_edit.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fab_edit.startAnimation(fromBottom)
            fab_scan.startAnimation(fromBottom)
        } else {
            fab_edit.startAnimation(toBottom)
            fab_scan.startAnimation(toBottom)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            fab_scan.isClickable = true
            fab_edit.isClickable = true
        } else {
            fab_scan.isClickable = false
            fab_edit.isClickable = false
        }
    }

    private lateinit var currentDrive: String

    private fun setDrive(d: String) {
        currentDrive = d
    }

    private fun sendCommandToService(action: String) =
        Intent(baseContext, TrackingService::class.java).also {
            it.action = action
            baseContext.startService(it)
        }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_CARD_FRAG) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, driveFragment, "fragmentTag")
            fragmentTransaction.commit()
        }
    }

    //GPS
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadLocation(locationResult: LocationResult) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val uid = user.uid

            val db = Firebase.firestore

            // get
            val s = (this.application as MyApplication).getDriveId()

            val lastLocation = LastLocation(
                locationResult.lastLocation?.accuracy,
                locationResult.lastLocation?.altitude,
                locationResult.lastLocation?.latitude,
                locationResult.lastLocation?.longitude,
                locationResult.lastLocation?.provider,
                locationResult.lastLocation?.speed,
                locationResult.lastLocation?.speedAccuracyMetersPerSecond,
                locationResult.lastLocation?.time,
                locationResult.lastLocation?.verticalAccuracyMeters
            )
            val locationPoint = LocationPoint(
                locationResult.locations[0].accuracy,
                locationResult.locations[0].altitude,
                locationResult.locations[0].latitude,
                locationResult.locations[0].longitude,
                locationResult.locations[0].provider,
                locationResult.locations[0].speed,
                locationResult.locations[0].speedAccuracyMetersPerSecond,
                locationResult.locations[0].time,
                locationResult.locations[0].verticalAccuracyMeters
            )

            val resultLocation = LocationResultSelf(lastLocation, locationPoint)

            if (s != "null") {
                val location = Location(resultLocation, uid, s!!)

                db.collection("locations")
                    .add(location)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            ContentValues.TAG,
                            "DocumentSnapshot added with ID: ${documentReference.id}"
                        )

                    }
                    .addOnFailureListener { e ->
                        Log.w(ContentValues.TAG, "Error adding document", e)

                    }
            }
        }
    }


    private fun startLocationUpdates() {
        Log.d(">>>>>>>>>>>>>>>", "startLocationUpdates")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    permissionsFineLocation
                )
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    permissionsFineLocation
                )
            }
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, null)
        updateGPS()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            permissionsFineLocation -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                updateGPS()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Diese Funktion benötigt eine Zustimmung um zu funktionieren",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateGPS() {
        Log.d(">>>>>>>>>>>>>>>", "updateGPS")
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
                    (this.application as MyApplication).setCurrentLocation(currentLocation)
                    Log.d("Current Location:", currentLocation.toString());
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    permissionsFineLocation
                )
                requestPermissions(
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                    permissionsFineLocation
                )
            }
        }
    }

}