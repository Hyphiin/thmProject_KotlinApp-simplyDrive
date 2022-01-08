package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidmads.library.qrgenearator.QRGEncoder
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.*
import de.thm.mow.felixwegener.simplydrive.services.TrackingService
import kotlinx.android.synthetic.main.activity_gps.*
import java.lang.Exception

private const val ARG_DATE = "date"
private const val ARG_DEP = "departure"
private const val ARG_STARTTIME = "departureTime"
private const val ARG_TRAVELTIME = "travelTime"
private const val ARG_STARTLON = "startLon"
private const val ARG_STARTLAT = "endLocation"
private const val ARG_LONARRAY = "LonArray"
private const val ARG_LATARRAY = "LatArray"


class CardDriveFragment : Fragment(), OnMapReadyCallback {
    private var currentLocation: android.location.Location? = null
    private var date: String? = null
    private var departure: String? = null
    private var startTime: String? = null
    private var travelTime: String? = null

    private lateinit var mMap: GoogleMap
    private var startLon: Double? = null
    private var startLat: Double? = null
    var posMarker: Marker? = null

    private var isTracking = true
    private var pathPoints= mutableListOf<MutableList<LatLng>>()

    private var lonArray: DoubleArray? = null
    private var latArray: DoubleArray? = null

    var bitmap: Bitmap? = null
    var qrgEncoder: QRGEncoder? = null

    private var qrCodeIV: ImageView? = null

    private lateinit var exportBtnTxt: FloatingActionButton
    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var routePoints: MutableList<Location>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = it.getString(ARG_DATE)
            departure = it.getString(ARG_DEP)
            startTime = it.getString(ARG_STARTTIME)
            travelTime = it.getString(ARG_TRAVELTIME)
            startLon = it.getDouble(ARG_STARTLON)
            startLat = it.getDouble(ARG_STARTLAT)
            lonArray = it.getDoubleArray(ARG_LONARRAY)
            latArray = it.getDoubleArray(ARG_LATARRAY)
        }

        currentLocation = (activity?.application as MyApplication).getCurrentLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_card_drive, container, false)

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

    private fun getMarkerIcon(color: String?): BitmapDescriptor? {
        val hsv = FloatArray(3)
        Color.colorToHSV(Color.parseColor(color), hsv)
        return BitmapDescriptorFactory.defaultMarker(hsv[0])
    }

    companion object {
        @JvmStatic
        fun newInstance(
            date: String,
            departure: String,
            startTime: String,
            travelTime: String,
            startLon: Double,
            startLat: Double,
            lonArray: DoubleArray,
            latArray: DoubleArray
        ) =
            CardDriveFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATE, date)
                    putString(ARG_DEP, departure)
                    putString(ARG_STARTTIME, startTime)
                    putString(ARG_TRAVELTIME, travelTime)
                    putDouble(ARG_STARTLON, startLon)
                    putDouble(ARG_STARTLAT, startLat)
                    putDoubleArray(ARG_LONARRAY, lonArray)
                    putDoubleArray(ARG_LATARRAY, latArray)
                }
            }
    }
}