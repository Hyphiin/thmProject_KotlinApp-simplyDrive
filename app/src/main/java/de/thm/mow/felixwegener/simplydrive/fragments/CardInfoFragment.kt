package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.zxing.WriterException
import de.thm.mow.felixwegener.simplydrive.R

private const val ARG_DEP = "departure"
private const val ARG_DES = "destination"
private const val ARG_STARTTIME = "departureTime"
private const val ARG_ENDTIME = "destinationTime"
private const val ARG_STARTLON = "startLon"
private const val ARG_STARTLAT = "endLocation"
private const val ARG_LONARRAY = "LonArray"
private const val ARG_LATARRAY = "LatArray"


class CardInfoFragment : Fragment(), OnMapReadyCallback {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
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

        val tvDepInfo = view.findViewById<TextView>(R.id.tvDepInfo)
        val tvDesInfo = view.findViewById<TextView>(R.id.tvDesInfo)
        val tvStartTime = view.findViewById<TextView>(R.id.tvStartTime)
        val tvEndTime = view.findViewById<TextView>(R.id.tvEndTime)

        qrCodeIV = view.findViewById(R.id.idIVQrcode)


        tvDepInfo.text = departure
        tvDesInfo.text = destination
        tvStartTime.text = startTime
        tvEndTime.text = endTime

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
        qrgEncoder = QRGEncoder ("Fahrt um $startTime Uhr von $departure"
            .toString(), null, QRGContents.Type.TEXT, dimen);
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
        transaction.add(de.thm.mow.felixwegener.simplydrive.R.id.map, fragment)
        transaction.commit()
        fragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        var idx = 0

        // Add a marker and move the camera
        latArray?.forEach { entry ->

            val tempLon = lonArray?.get(idx)
            val location = LatLng(entry, tempLon!!)
            mMap.addMarker(MarkerOptions().position(location).title("Position: ${location.latitude} ; ${location.longitude}"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            val markerOptions = MarkerOptions()
            markerOptions.position(location)
            markerOptions.title("Lat:" + location.latitude + " Lon:" + location.longitude)
            mMap.addMarker(markerOptions)

            idx++
        }


        /*
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        val markerOptions = MarkerOptions()
        markerOptions.position(sydney)
        markerOptions.title("Lat:"+ sydney.latitude + " Lon:" + sydney.longitude)
        mMap.addMarker(markerOptions)*/
    }

    companion object {
        @JvmStatic
        fun newInstance(
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