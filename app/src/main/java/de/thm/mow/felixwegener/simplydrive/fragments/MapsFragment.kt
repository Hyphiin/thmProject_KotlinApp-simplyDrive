package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.*
import de.thm.mow.felixwegener.simplydrive.services.TrackingService
import java.lang.Exception
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptor


private const val ARG_START = "start"
private const val ARG_END = "end"

class MapsFragment : Fragment(), OnMapReadyCallback{

    private var mMap: GoogleMap? = null
    private var start: DoubleArray? = null
    private var end: DoubleArray? = null
    var posMarker: Marker? = null

    private var isTracking = true
    private var pathPoints= mutableListOf<MutableList<LatLng>>()

    private var manageVar = 0


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

        subscribeToObservers()

        return view
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)

        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestMarker()
            moveCameraToUser()
        })
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
    }

    private fun moveCameraToUser() {
        Log.d("VAR", "BEFORE: $manageVar")
        if (manageVar <= 2) {
            if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
                mMap?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        pathPoints.last().last(),
                        Constants.MAP_ZOOM
                    )
                )
            }
            manageVar ++

        }
        Log.d("VAR", "AFTER: $manageVar")

    }

    private fun addLatestMarker() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            posMarker?.remove()
            val lastLatLng = pathPoints.last().last()

            val markerOptions = MarkerOptions()
            markerOptions.position(lastLatLng)
            val geocoder = Geocoder(context)
            try {
                val adresses: List<Address> =
                    geocoder.getFromLocation(
                        lastLatLng.latitude,
                        lastLatLng.longitude,
                        1
                    )
                markerOptions.title(adresses[0].getAddressLine(0))
            } catch (e: Exception) {
                markerOptions.title("Lat: " + lastLatLng.latitude + ", Lon: " + lastLatLng.longitude)
            }

            posMarker = mMap!!.addMarker(markerOptions)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val markerOptions = MarkerOptions()

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
                markerOptions.title("Lat: " + latLng.latitude + ", Lon: " + latLng.longitude)
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
                            Log.d("TEST!!!!!!","${entry.data!!.values.first()}")
                            station = Station(
                                entry.data!!.values.first() as Double?,
                                entry.data!!.values.last() as Double?
                            )

                            stationName = entry.id
                        }

                        val latLng = LatLng(station.latitude!!, station.longitude!!)
                        markerOptions.position(latLng)
                        markerOptions.icon(getMarkerIcon("#65FADD"))

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

    private fun getMarkerIcon(color: String?): BitmapDescriptor? {
        val hsv = FloatArray(3)
        Color.colorToHSV(Color.parseColor(color), hsv)
        return BitmapDescriptorFactory.defaultMarker(hsv[0])
    }

}

