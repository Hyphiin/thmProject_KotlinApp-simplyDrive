package de.thm.mow.felixwegener.simplydrive.fragments

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import de.thm.mow.felixwegener.simplydrive.MyApplication


private const val ARG_START = "start"
private const val ARG_END = "end"

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var start: DoubleArray? = null
    private var end: DoubleArray? = null
    private lateinit var currentLocation: Location
    private var currLocTrue: Boolean = false


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

        currLocTrue = (activity?.application as MyApplication).getCurrLocTrue()

        if (currLocTrue){
            currentLocation = (activity?.application as MyApplication).getCurrentLocation()
        }

        return view
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (currLocTrue) {
            val currLocation = LatLng(currentLocation.latitude, currentLocation.longitude)
            val markerOptions = MarkerOptions()
            markerOptions.position(currLocation)
            markerOptions.title("Lat:" + currLocation.latitude + " Lon:" + currLocation.longitude)
            mMap.addMarker(markerOptions)
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
}

