package de.thm.mow.felixwegener.simplydrive.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.thm.mow.felixwegener.simplydrive.R

private const val ARG_DEP = "departure"
private const val ARG_DES = "destination"
private const val ARG_STARTTIME = "departureTime"
private const val ARG_ENDTIME = "destinationTime"
private const val ARG_STARTLOC = "startLocation"
private const val ARG_ENDLOC = "endLocation"

class CardInfoFragment : Fragment(), OnMapReadyCallback {
    private var departure: String? = null
    private var destination: String? = null
    private var startTime: String? = null
    private var endTime: String? = null

    private lateinit var mMap: GoogleMap
    private var startLocation: Double? = null
    private var endLocation: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            departure = it.getString(ARG_DEP)
            destination = it.getString(ARG_DES)
            startTime = it.getString(ARG_STARTTIME)
            endTime = it.getString(ARG_ENDTIME)
            startLocation = it.getDouble(ARG_STARTLOC)
            endLocation = it.getDouble(ARG_ENDLOC)
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

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val location = LatLng(startLocation!!, endLocation!!)
        mMap.addMarker(MarkerOptions().position(location).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
        val markerOptions = MarkerOptions()
        markerOptions.position(location)
        markerOptions.title("Lat:"+ location.latitude + " Lon:" + location.longitude)
        mMap.addMarker(markerOptions)

    }

    companion object {
        @JvmStatic
        fun newInstance(departure: String, destination: String, startTime: String) =
            CardInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEP, departure)
                    putString(ARG_DES, destination)
                    putString(ARG_STARTTIME, startTime)
                    //putDouble(ARG_STARTLOC, startLocation)
                    //putDouble(ARG_ENDLOC, endLocation)
                }
            }
    }
}