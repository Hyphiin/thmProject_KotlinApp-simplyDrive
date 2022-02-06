package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidx.fragment.app.Fragment
import androidmads.library.qrgenearator.QRGEncoder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.zxing.WriterException
import de.thm.mow.felixwegener.simplydrive.*
import de.thm.mow.felixwegener.simplydrive.services.TrackingService
import kotlinx.android.synthetic.main.fragment_card_drive.*
import java.lang.Exception

private const val ARG_DATE = "date"
private const val ARG_DEP = "departure"
private const val ARG_STARTTIME = "departureTime"

class CardDriveFragment : Fragment(), OnMapReadyCallback {
    private var currentLocation: android.location.Location? = null
    private var date: String? = null
    private var departure: String? = null
    private var startTime: String? = null

    private var mMap: GoogleMap? = null

    var posMarker: Marker? = null

    private var isTracking = true
    private var activeRoute = true
    private var pathPoints= mutableListOf<MutableList<LatLng>>()

    var bitmap: Bitmap? = null
    var qrgEncoder: QRGEncoder? = null

    private var qrCodeIV: ImageView? = null

    private var curTimeMillis = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = it.getString(ARG_DATE)
            departure = it.getString(ARG_DEP)
            startTime = it.getString(ARG_STARTTIME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_card_drive, container, false)

        val tvDate = view.findViewById<TextView>(R.id.tvLHdate)
        val tvDepInfo = view.findViewById<TextView>(R.id.tvDepInfo)
        val tvStartTime = view.findViewById<TextView>(R.id.tvStartTime)

        tvDepInfo.text = departure
        tvStartTime.text = startTime
        tvDate.text = date

        qrCodeIV = view.findViewById(R.id.idIVQrcode)

        // for generating Ticket
        // below line is for getting
        // the windowmanager service.
        // below line is for getting
        // the windowmanager service.
        val managerWindow = activity?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?

        // initializing a variable for default display.

        // initializing a variable for default display.
        val display: Display = managerWindow!!.defaultDisplay

        // creating a variable for point which
        // is to be displayed in QR Code.

        // creating a variable for point which
        // is to be displayed in QR Code.
        val point = Point()
        display.getSize(point)

        // getting width and
        // height of a point

        // getting width and
        // height of a point
        val width: Int = point.x
        val height: Int = point.y

        // generating dimension from width and height.

        // generating dimension from width and height.
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4


        // ToDo overthink, where to integrate
        qrgEncoder = QRGEncoder(
            "Fahrt um $startTime Uhr von $departure"
                .toString(), null, QRGContents.Type.TEXT, dimen
        );
        try {
            // getting our qrcode in the form of bitmap.
            bitmap = qrgEncoder!!.encodeAsBitmap();
            // the bitmap is set inside our image
            // view using .setimagebitmap method.
            qrCodeIV?.setImageBitmap(bitmap);
        } catch (e: WriterException) {
            // this method is called for
            // exception handling.
            Log.e("Tag", e.toString());
        }

        val manager = fragmentManager
        val transaction = manager!!.beginTransaction()
        val fragment = SupportMapFragment()
        transaction.add(de.thm.mow.felixwegener.simplydrive.R.id.mapView, fragment)
        transaction.commit()
        fragment.getMapAsync(this)

        subscribeToObservers()
        TrackingService.activeRoute = MutableLiveData(true)

       return view
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.activeRoute.observe(viewLifecycleOwner, Observer {
            updateTrackingRoute(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeInMillis.observe(viewLifecycleOwner, Observer {
            curTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeMillis,true)
            timer.text = formattedTime
        })
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
    }

    private fun updateTrackingRoute(activeRoute: Boolean){
        this.activeRoute = activeRoute
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
                    Log.e(ContentValues.TAG, "get failed with ", exception)
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
        ) =
            CardDriveFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATE, date)
                    putString(ARG_DEP, departure)
                    putString(ARG_STARTTIME, startTime)
                }
            }
    }
}