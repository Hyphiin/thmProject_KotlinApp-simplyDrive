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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.WriterException
import de.thm.mow.felixwegener.simplydrive.Location
import de.thm.mow.felixwegener.simplydrive.R
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.Exception

private const val ARG_DATE = "date"
private const val ARG_DEP = "departure"
private const val ARG_DES = "destination"
private const val ARG_STARTTIME = "departureTime"
private const val ARG_ENDTIME = "destinationTime"
private const val ARG_STARTLON = "startLon"
private const val ARG_STARTLAT = "endLocation"
private const val ARG_LONARRAY = "LonArray"
private const val ARG_LATARRAY = "LatArray"


class CardInfoFragment : Fragment(), OnMapReadyCallback {
    private var date: String? = null
    private var departure: String? = null
    private var destination: String? = null
    private var startTime: String? = null
    private var endTime: String? = null

    private lateinit var mMap: GoogleMap
    private var startLon: Double? = null
    private var startLat: Double? = null

    private var lonArray: DoubleArray? = null
    private var latArray: DoubleArray? = null

    var bitmap: Bitmap? = null
    var qrgEncoder: QRGEncoder? = null

    private var qrCodeIV: ImageView? = null

    private lateinit var closeBtn: Button

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
            endTime = it.getString(ARG_ENDTIME)
            startLon = it.getDouble(ARG_STARTLON)
            startLat = it.getDouble(ARG_STARTLAT)
            lonArray = it.getDoubleArray(ARG_LONARRAY)
            latArray = it.getDoubleArray(ARG_LATARRAY)
        }
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
        //val tvEndTime = view.findViewById<TextView>(R.id.tvEndTime)
        closeBtn = view.findViewById(R.id.btn__closeCard)
        exportBtnTxt = view.findViewById(R.id.btn__ExportTxt)

        qrCodeIV = view.findViewById(R.id.idIVQrcode)


        tvDepInfo.text = departure
        tvDesInfo.text = destination
        tvStartTime.text = startTime
        tvDate.text = date
        //tvEndTime.text = endTime

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

        exportBtnTxt.setOnClickListener {
            date?.let { it1 -> startTime?.let { it2 -> saveTxt(it1, it2) } }
        }

        val manager = fragmentManager
        val transaction = manager!!.beginTransaction()
        val fragment = SupportMapFragment()
        transaction.add(de.thm.mow.felixwegener.simplydrive.R.id.map, fragment)
        transaction.commit()
        fragment.getMapAsync(this)
        return view
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
                        Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                        foundItems.add(document.id)
                    }

                    currentRoute = foundItems.first()

                    databaseRef.collection("locations")
                        .whereEqualTo("uid", user.uid)
                        .whereEqualTo("routeId", currentRoute)
                        .get()
                        .addOnSuccessListener { points ->
                            for (point in points) {
                                Log.d(ContentValues.TAG, "$point => $point")
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
                                    outputStreamWriter.write(routePoints.toString())
                                    outputStreamWriter.close()
                                    Toast.makeText(activity, "Datei mit dem Namen ${currentRoute}.txt \nerfolgreich heruntergeladen", Toast.LENGTH_SHORT).show()
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
        var idx = 0

        // Add a marker and move the camera
        latArray?.forEach { entry ->

            val tempLon = lonArray?.get(idx)
            val location = LatLng(entry, tempLon!!)
            mMap.addMarker(
                MarkerOptions().position(location)
                    .title("Position: ${location.latitude} ; ${location.longitude}")
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            val markerOptions = MarkerOptions()
            markerOptions.position(location)
            val geocoder = Geocoder(context)
            try {
                val adresses: List<Address> =
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                markerOptions.title(adresses[0].getAddressLine(0))
            } catch (e: Exception) {
                markerOptions.title("Lat: "+location.latitude + ", Lon: " + location.longitude)
            }
            mMap.addMarker(markerOptions)

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15F));
            // Zoom in, animating the camera.
            googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15F), 2000, null);

            idx++
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
            lonArray: DoubleArray,
            latArray: DoubleArray
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
                    putDoubleArray(ARG_LONARRAY, lonArray)
                    putDoubleArray(ARG_LATARRAY, latArray)
                }
            }
    }
}