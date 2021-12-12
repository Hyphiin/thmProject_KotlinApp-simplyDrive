package de.thm.mow.felixwegener.simplydrive.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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

        tvDepInfo.text = departure
        tvDesInfo.text = destination
        tvStartTime.text = startTime
        tvEndTime.text = endTime

        val manager = fragmentManager
        val transaction = manager!!.beginTransaction()
        val fragment = SupportMapFragment()
        transaction.add(de.thm.mow.felixwegener.simplydrive.R.id.map, fragment)
        transaction.commit()
        fragment.getMapAsync(this)
        return view

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        var idx = 0
        var plus = 0.0

        // Add a marker and move the camera
        latArray?.forEach { entry ->

            val tempLon = lonArray?.get(idx)
            val location = LatLng(entry + plus, tempLon!! + plus)
            mMap.addMarker(MarkerOptions().position(location).title("StartMarker"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
            val markerOptions = MarkerOptions()
            markerOptions.position(location)
            markerOptions.title("Lat:" + location.latitude + " Lon:" + location.longitude)
            mMap.addMarker(markerOptions)

            idx++
            plus += 0.1
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