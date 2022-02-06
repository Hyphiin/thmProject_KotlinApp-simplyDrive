package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.zxing.WriterException
import de.thm.mow.felixwegener.simplydrive.Constants
import de.thm.mow.felixwegener.simplydrive.Constants.MAP_ZOOM
import de.thm.mow.felixwegener.simplydrive.Location
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import de.thm.mow.felixwegener.simplydrive.services.TrackingService
import kotlinx.android.synthetic.main.fragment_card_info.*
import kotlinx.coroutines.awaitAll
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.Exception
import java.lang.StringBuilder
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_DATE = "date"
private const val ARG_DEP = "departure"
private const val ARG_DES = "destination"
private const val ARG_STARTTIME = "departureTime"
private const val ARG_STARTLON = "startLon"
private const val ARG_STARTLAT = "endLocation"
private const val ARG_USERID = "userId"
private const val ARG_ROUTETIME = "routeTime"


class CardInfoFragment : Fragment(), OnMapReadyCallback {
    private var date: String? = null
    private var departure: String? = null
    private var destination: String? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private var timerTime: String? = null
    private var startLocTime: String? = null

    private var mMap: GoogleMap? = null
    private var startLon: Double? = null
    private var startLat: Double? = null

    private lateinit var mAuth: FirebaseAuth
    private var userId: String? = null
    private var routeTime: String? = null

    private var isTracking = true
    private var pathPoints= mutableListOf<MutableList<LatLng>>()

    private var lonArray: DoubleArray? = null
    private var latArray: DoubleArray? = null

    var bitmap: Bitmap? = null
    private var qrgEncoder: QRGEncoder? = null

    private var qrCodeIV: ImageView? = null

    private lateinit var closeBtn: Button

    private lateinit var deleteBtn: FloatingActionButton

    private lateinit var exportBtnTxt: FloatingActionButton
    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var routePoints: MutableList<Location>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            date = it.getString(ARG_DATE)
            departure = it.getString(ARG_DEP)
            destination = it.getString(ARG_DES)
            startTime = it.getString(ARG_STARTTIME)
            startLon = it.getDouble(ARG_STARTLON)
            startLat = it.getDouble(ARG_STARTLAT)
            userId = it.getString(ARG_USERID)
            routeTime = it.getString(ARG_ROUTETIME)
        }
        getRouteData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_card_info, container, false)

        val tvDate = view.findViewById<TextView>(R.id.tvLHdate)
        val tvDepInfo = view.findViewById<TextView>(R.id.tvDepInfo)
        val tvDesInfo = view.findViewById<TextView>(R.id.tvDesInfo)
        val tvStartTime = view.findViewById<TextView>(R.id.tvStartTime)
        closeBtn = view.findViewById(R.id.btn__closeCard)
        exportBtnTxt = view.findViewById(R.id.btn__ExportTxt)
        deleteBtn = view.findViewById(R.id.deleteBTN)

        qrCodeIV = view.findViewById(R.id.idIVQrcode)


        tvDepInfo.text = departure
        tvDesInfo.text = destination
        tvDate.text = date


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

        closeBtn.setOnClickListener {
            activity?.onBackPressed()
        }

        deleteBtn.setOnClickListener {
            deleteLocations()
            activity?.onBackPressed()
        }

        exportBtnTxt.setOnClickListener {
            date?.let { it1 -> startTime?.let { it2 -> saveTxt(it1, it2) } }
        }

        val manager = fragmentManager
        val transaction = manager!!.beginTransaction()
        val fragment = SupportMapFragment()
        transaction.add(R.id.map, fragment)
        transaction.commit()
        fragment.getMapAsync(this)

        return view
    }


    private fun moveCameraToUser() {
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            mMap?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.first().first(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines() {
        var oldPoly = pathPoints.first().first()
        var newPoly: LatLng

        for (polyline in pathPoints) {
            newPoly = LatLng(polyline.first().latitude, polyline.last().longitude)

            val polylineOptions = PolylineOptions()
                .color(Constants.POLYLINE_COLOR)
                .width(Constants.POLYLINE_WIDTH)
                .add(oldPoly)
                .add(newPoly)
            mMap?.addPolyline(polylineOptions)
            oldPoly = newPoly
        }
        val markerOptions = MarkerOptions()

        if(pathPoints.isNotEmpty()) {
            val firstLatLng = pathPoints.first().first()
            markerOptions.position(firstLatLng)
            markerOptions.title(departure)
            mMap!!.addMarker(markerOptions)
            val lastLatLng = pathPoints.last().last()
            markerOptions.position(lastLatLng)
            markerOptions.title(destination)
            mMap!!.addMarker(markerOptions)
        }
        moveCameraToUser()
    }


    private fun getRouteData() {
        var currentRoute: String
        routePoints = mutableListOf()

        databaseRef = FirebaseFirestore.getInstance()

        mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

        if (firebaseUser != null) {
            userId = firebaseUser.uid
        }

        val foundItems = mutableListOf<String>()

        databaseRef.collection("routes").whereEqualTo("uid", userId)
            .whereEqualTo("time", routeTime.toString()).whereEqualTo("date", date.toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    foundItems.add(document.id)
                }
                    currentRoute = foundItems.first()

                    databaseRef.collection("locations")
                        .whereEqualTo("uid", userId)
                        .whereEqualTo("routeId", currentRoute)
                        .get()
                        .addOnSuccessListener { points ->
                            for (point in points) {
                                routePoints.add(point.toObject(Location::class.java))
                            }
                            routePoints.sortBy { location: Location -> location.location?.lastLocation?.time }

                            val firstPathPointObject = routePoints.first()
                            val lastPathPointObject = routePoints.last()
                            val firstTime = Timestamp(firstPathPointObject.location?.locations?.time!!)
                            val lastTime = Timestamp(lastPathPointObject.location?.locations?.time!!)
                            val tempTimerTime = Timestamp(lastTime.time - firstTime.time)

                            startLocTime = "${firstTime.hours}:${firstTime.minutes}:${if(firstTime.seconds < 10) "0" else ""}${firstTime.seconds}"
                            endTime = "${lastTime.hours}:${lastTime.minutes}:${if(lastTime.seconds < 10) "0" else ""}${lastTime.seconds}"
                            timerTime = "${tempTimerTime.hours}:${tempTimerTime.minutes}:${if(tempTimerTime.seconds < 10) "0" else ""}${tempTimerTime.seconds}"

                            tvStartTime.text = startTime
                            tvEndTime.text = endTime
                            tvTimerTime.text = timerTime


                            if (routePoints.isNotEmpty()) {
                                val firstLoc = routePoints.first().location?.locations
                                lonArray = DoubleArray(routePoints.size)
                                latArray = DoubleArray(routePoints.size)

                                var idx = 0
                                routePoints.forEach { entry ->
                                    val tempLon = idx
                                    val tempLat = idx

                                    if (entry != null) {
                                        lonArray!![tempLon] =
                                            entry.location?.locations?.longitude!!
                                    }
                                    if (entry != null) {
                                        latArray!![tempLat] =
                                            entry.location?.locations?.latitude!!
                                    }
                                    idx++
                                }
                                for (location in routePoints) {
                                    pathPoints.add(mutableListOf(LatLng(location.location?.locations?.latitude!!,location.location?.locations?.longitude!!)))
                                }
                                addAllPolylines()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                        }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }

    private fun saveTxt(date: String, time: String) {
        databaseRef = FirebaseFirestore.getInstance()
        var currentRoute: String
        routePoints = mutableListOf()
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val foundItems = mutableListOf<String>()

            databaseRef.collection("routes").whereEqualTo("uid", user.uid)
                .whereEqualTo("time", time.toString()).whereEqualTo("date", date.toString())
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        foundItems.add(document.id)
                    }

                    currentRoute = foundItems.first()

                    databaseRef.collection("locations")
                        .whereEqualTo("uid", user.uid)
                        .whereEqualTo("routeId", currentRoute)
                        .get()
                        .addOnSuccessListener { points ->
                            for (point in points) {
                                routePoints.add(point.toObject(Location::class.java))
                            }

                            if (routePoints.isNotEmpty()) {

                                try {
                                    val outputStreamWriter = OutputStreamWriter(
                                        context?.openFileOutput(
                                            "${currentRoute}.txt",
                                            Context.MODE_PRIVATE
                                        )
                                    )
                                    val jsonString = Gson().toJson(routePoints)  // json string
                                    outputStreamWriter.write(jsonString)
                                    outputStreamWriter.close()
                                    Toast.makeText(
                                        activity,
                                        "Datei mit dem Namen ${currentRoute}.txt \nerfolgreich heruntergeladen",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: IOException) {
                                    Log.e("Exception", "File write failed: $e")
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                        }

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }



    private fun deleteLocations() {

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user!!.uid


        val db = Firebase.firestore

        var docId: String? = "leer"

        db.collection("routes").whereEqualTo("date", date).whereEqualTo("time", startTime).get().addOnSuccessListener { result ->
            for (document in result) {
                docId = document.id
                document.reference.delete()

                if (docId != "leer") {
                    db.collection("locations").whereEqualTo("routeId", docId).get().addOnSuccessListener { result ->
                        for (document in result) {
                            document.reference.delete()
                        }
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(
            date: String,
            departure: String,
            destination: String,
            startTime: String,
            startLon: Double,
            startLat: Double,
            userId: String,
            routeTime: String
        ) =
            CardInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATE, date)
                    putString(ARG_DEP, departure)
                    putString(ARG_DES, destination)
                    putString(ARG_STARTTIME, startTime)
                    //putString(ARG_ENDTIME, endTime)
                    putDouble(ARG_STARTLON, startLon)
                    putDouble(ARG_STARTLAT, startLat)
                    putString(ARG_USERID, userId)
                    putString(ARG_ROUTETIME, routeTime)
                }
            }
    }
}