package de.thm.mow.felixwegener.simplydrive.fragments

import android.Manifest
import android.content.ContentValues
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
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.lifecycle.Observer
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.*
import de.thm.mow.felixwegener.simplydrive.Constants.POLYLINE_COLOR
import de.thm.mow.felixwegener.simplydrive.Constants.POLYLINE_WIDTH
import de.thm.mow.felixwegener.simplydrive.services.TrackingService
import kotlinx.android.synthetic.main.fragment_maps.*
import java.lang.Exception
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.ktx.toObject
import de.thm.mow.felixwegener.simplydrive.R
import kotlin.math.abs


private const val ARG_START = "start"
private const val ARG_END = "end"

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var currentLocation: Location? = null

    private var mMap: GoogleMap? = null
    private var start: DoubleArray? = null
    private var end: DoubleArray? = null
    var posMarker: Marker? = null

    private var isTracking = true
    private var pathPoints= mutableListOf<MutableList<LatLng>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            start = it.getDoubleArray(ARG_START)
            end = it.getDoubleArray(ARG_END)
        }

        currentLocation = (activity?.application as MyApplication).getCurrentLocation()
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

        subscribeToObservers()

        return view
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
    }

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    Constants.MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .addAll(polyline)
            mMap?.addPolyline(polylineOptions)
        }
        val markerOptions = MarkerOptions()
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            posMarker?.remove()
            val lastLatLng = pathPoints.first().first()
            markerOptions.position(lastLatLng)

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
            posMarker = mMap!!.addMarker(markerOptions)
        }
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            posMarker?.remove()
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            mMap?.addPolyline(polylineOptions)

            val markerOptions = MarkerOptions()

            markerOptions.position(lastLatLng)

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

            posMarker = mMap!!.addMarker(markerOptions)
        }
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val markerOptions = MarkerOptions()

        addAllPolylines()

        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val latLng = pathPoints.first().first()
            markerOptions.position(latLng)
            val geocoder = Geocoder(context)
            try {
                val adresses: List<Address> =
                    geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    )
                markerOptions.title(adresses[0].getAddressLine(0))
            } catch (e: Exception) {
                markerOptions.title("Lat: " + currentLocation?.latitude + ", Lon: " + currentLocation?.longitude)
            }
            mMap!!.addMarker(markerOptions)
        }


        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = Firebase.firestore

            lateinit var station: Station
            var stationName: String = "Test"

            db.collection("stations").get()
                .addOnSuccessListener { document ->
                    for (entry in document.documents) {
                        if (entry.data?.isNotEmpty() == true){
                            station = Station(
                                entry.data!!.values.first() as Double?,
                                entry.data!!.values.last() as Double?
                            )

                            stationName = entry.id
                        }

                        val latLng = LatLng(station.latitude!!, station.longitude!!)
                        markerOptions.position(latLng)
                        markerOptions.icon(
                            BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                        )

                        markerOptions.title(stationName)

                        mMap!!.addMarker(markerOptions)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "get failed with ", exception)
                }
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

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

}

