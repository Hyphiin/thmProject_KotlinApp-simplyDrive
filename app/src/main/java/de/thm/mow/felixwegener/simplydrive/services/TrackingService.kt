package de.thm.mow.felixwegener.simplydrive.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.*
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_PAUSE_SERVICE
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_SHOW_CARD_FRAG
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_START_OR_RESUME_SERVICE
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_STOP_SERVICE
import de.thm.mow.felixwegener.simplydrive.Constants.FASTEST_LOCATION_INTERVAL
import de.thm.mow.felixwegener.simplydrive.Constants.LOCATION_UPDATE_INTERVAL
import de.thm.mow.felixwegener.simplydrive.Constants.NOTIFICATION_CHANNEL_ID
import de.thm.mow.felixwegener.simplydrive.Constants.NOTIFICATION_CHANNEL_NAME
import de.thm.mow.felixwegener.simplydrive.Constants.NOTIFICATION_ID

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {

    var isFirstRun = true

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //private var currentLocation : android.location.Location? = null

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        Toast.makeText(
                            this@TrackingService,
                            "FirstStart",
                            Toast.LENGTH_SHORT
                        ).show()
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Toast.makeText(
                            this@TrackingService,
                            "Resuming Service",
                            Toast.LENGTH_SHORT
                        ).show()
                        //has to be changed
                        startForegroundService()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Toast.makeText(
                        this@TrackingService,
                        "Paused",
                        Toast.LENGTH_SHORT
                    ).show()
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Toast.makeText(
                        this@TrackingService,
                        "Stopped",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> Toast.makeText(
                    this@TrackingService,
                    "Upsi",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
                        Log.d("TAG", "IS TRACKING: ${location.latitude} + ${location.longitude}")
                    }
                }
            }
            uploadLocation(result)
        }
    }

    private fun addPathPoint(location: android.location.Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
            (application as MyApplication).setCurrentLocation(location)
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("SimplyDrive")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_CARD_FRAG
        },
        FLAG_MUTABLE
    )

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel (notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

}