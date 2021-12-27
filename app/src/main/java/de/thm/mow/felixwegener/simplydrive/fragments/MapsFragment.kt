package de.thm.mow.felixwegener.simplydrive.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.thm.mow.felixwegener.simplydrive.MyApplication
import java.lang.Exception


private const val ARG_START = "start"
private const val ARG_END = "end"

class MapsFragment : Fragment(), OnMapReadyCallback {

    private val defaultUpdateInterval = 30
    private val fastUpdateInterval = 5
    private val permissionsFineLocation = 99

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallBack: LocationCallback

    private var currentLocation: Location? = null

    private lateinit var mMap: GoogleMap
    private var start: DoubleArray? = null
    private var end: DoubleArray? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            start = it.getDoubleArray(ARG_START)
            end = it.getDoubleArray(ARG_END)
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(de.thm.mow.felixwegener.simplydrive.R.layout.fragment_maps, container, false)
        val manager = fragmentManager
        val transaction = manager!!.beginTransaction()
        val fragment = SupportMapFragment()
        transaction.add(de.thm.mow.felixwegener.simplydrive.R.id.mapView, fragment)
        transaction.commit()
        fragment.getMapAsync(this)

        locationRequest = LocationRequest()

        locationRequest.interval = (1000 * defaultUpdateInterval).toLong()
        locationRequest.fastestInterval = (1000 * fastUpdateInterval).toLong()

        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        locationCallBack = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult?) {
                Log.d("Tracked Location:", locationResult.toString());
                locationResult ?: return
                /*for (location in locationResult.locations) {
                    updateGPS()
                    startLocationUpdates()

                }*/
                updateGPS()
                startLocationUpdates()
            }
        }
        updateGPS()
        startLocationUpdates()

        return view
    }

    /*override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }*/

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
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
                Log.d(":_:_:_:_:_:_:", "updateGPS")
                updateGPS()
            } else {
                Toast.makeText(
                    activity,
                    "Diese Funktion benötigt eine Zustimmung um zu funktionieren",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    private fun updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) === PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLocation = location
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


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        startLocationUpdates()
        updateGPS()
        val markerOptions = MarkerOptions()

        if (currentLocation != null) {
            val latLng = LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!)
            markerOptions.position(latLng)

            val geocoder = Geocoder(context)
            try {
                val adresses: List<Address> =
                    geocoder.getFromLocation(
                        currentLocation?.latitude!!,
                        currentLocation?.longitude!!,
                        1
                    )
                markerOptions.title(adresses[0].getAddressLine(0))
            } catch (e: Exception) {
                markerOptions.title("Lat: " + currentLocation?.latitude + ", Lon: " + currentLocation?.longitude)
            }
            mMap.addMarker(markerOptions)

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F));
            // Zoom in, animating the camera.
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15F), 2000, null);
        }

    }

    companion object {
        fun newInstance(start: DoubleArray, end: DoubleArray) =
            HistoryDetailView().apply {
                arguments = Bundle().apply {
                    putDoubleArray(ARG_START, start)
                    putDoubleArray(ARG_END, end)
                }
            }
    }
}

