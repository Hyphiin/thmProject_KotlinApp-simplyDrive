package de.thm.mow.felixwegener.simplydrive.fragments

import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidmads.library.qrgenearator.QRGEncoder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import de.thm.mow.felixwegener.simplydrive.Constants
import de.thm.mow.felixwegener.simplydrive.Location
import de.thm.mow.felixwegener.simplydrive.MyApplication
import de.thm.mow.felixwegener.simplydrive.R
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

       return view
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

    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.first().first(),
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
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            mMap.addPolyline(polylineOptions)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

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
}