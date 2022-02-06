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
import com.google.android.gms.location.LocationRequest.*
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
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
import de.thm.mow.felixwegener.simplydrive.Constants.TIMER_UPDATE_INTERVAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    companion object {
        val timeInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        var activeRoute = MutableLiveData<Boolean>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        activeRoute.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeInSeconds.postValue(0L)
        timeInMillis.postValue(0L)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    clearPathpoints()
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    clearPathpoints()
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                }
                else -> Toast.makeText(
                    this@TrackingService,
                    "Service konnte nicht richtig gestartet werden.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRoute = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L


    private fun clearPathpoints() = pathPoints?.apply {
        pathPoints.postValue(null)
    }

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeInMillis.postValue(timeRoute + lapTime)
                if (timeInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeInSeconds.postValue(timeInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRoute += lapTime
        }

    }

    private fun pauseService() {
        isTracking.postValue(false)
        activeRoute.postValue(false)
        isTimerEnabled = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if(isTracking) "Stop" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_IMMUTABLE)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            getService(this,2,resumeIntent, FLAG_IMMUTABLE)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(!serviceKilled) {
            currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
                isAccessible = true
                set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
                currentNotificationBuilder = baseNotificationBuilder
                    .addAction(1, notificationActionText, pendingIntent)
                notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermissions(this)){
                if ((application as MyApplication).getCellOnlyMode() == true){
                    val request = LocationRequest().apply {
                        interval = LOCATION_UPDATE_INTERVAL
                        fastestInterval = FASTEST_LOCATION_INTERVAL
                        priority = PRIORITY_LOW_POWER
                    }
                    fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } else if (((application as MyApplication).getTwoMode() == true)){
                    val request = LocationRequest().apply {
                        interval = LOCATION_UPDATE_INTERVAL
                        fastestInterval = FASTEST_LOCATION_INTERVAL
                        priority = PRIORITY_BALANCED_POWER_ACCURACY
                    }
                    fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } else {
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
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
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
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeInSeconds.observe(this, Observer {
            if(!serviceKilled) {
                if (activeRoute.value == true) {
                    val notification = currentNotificationBuilder
                        .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                    notificationManager.notify(NOTIFICATION_ID, notification.build())
                } else {
                    val notification = currentNotificationBuilder
                        .setContentText("is currently Tracking")
                    notificationManager.notify(NOTIFICATION_ID, notification.build())
                }
            }
        })
    }

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
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error adding document", e)

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