package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.*
import de.thm.mow.felixwegener.simplydrive.Constants.ACTION_STOP_SERVICE
import de.thm.mow.felixwegener.simplydrive.services.TrackingService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.withTimeout
import java.util.*
import kotlin.math.abs

class EditFragment : Fragment() {

    //lateinit var stationInput: EditText
    lateinit var lineInput: EditText
    lateinit var insertBtn: Button
    lateinit var stationSpinner: Spinner

    private lateinit var contextF: FragmentActivity
    private var startDrive: Boolean = true
    private lateinit var driveId: String
    private lateinit var startStation: String

    private var isTracking = true
    private var activeRoute = true

    private var currentLocation: Location? = null

    lateinit var dataPasser: OnDataPass

    private lateinit var databaseRef: FirebaseFirestore
    lateinit var items: Array<String?>

    interface OnDataPass {
        fun onDataPass(data: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = context as OnDataPass
    }

    fun passData(data: String) {
        dataPasser.onDataPass(data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLocation = (activity?.application as MyApplication).getCurrentLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        insertBtn = view.findViewById(R.id.btnInsert)
        //stationInput = view.findViewById(R.id.stationInput)
        lineInput = view.findViewById(R.id.lineInput)
        stationSpinner = view.findViewById(R.id.stationSpinner)

        getAllStations()

        stationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //stationInput.setText(items[position])
                startStation = items[position].toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //stationInput.setText("Bitte eine Station auswählen")
                startStation = "Bitte eine Station auswählen"
            }

        }

        val activity = requireActivity()
        contextF = activity
        driveId = (activity.application as MyApplication).getDriveId()!!
        startDrive = (activity.application as MyApplication).getStartDrive()!!

        insertBtn.setOnClickListener { view ->
            if (startDrive) {
                addHistory()
                TrackingService.activeRoute = MutableLiveData(true)
                (requireActivity().application as MyApplication).setStartDrive(false)
            } else {
                editHistory()
                sendCommandToService(ACTION_STOP_SERVICE)
                TrackingService.activeRoute = MutableLiveData(false)
                (requireActivity().application as MyApplication).setStartDrive(true)
            }
        }

        subscribeToObservers()
        return view
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.activeRoute.observe(viewLifecycleOwner, Observer {
            updateTrackingRoute(it)
        })
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
    }

    private fun updateTrackingRoute(activeRoute: Boolean){
        this.activeRoute = activeRoute
    }

    private fun getAllStations() {
        databaseRef = FirebaseFirestore.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val foundItems = mutableListOf<String>()

            databaseRef.collection("stations")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        foundItems.add(document.id)
                    }

                    items = arrayOfNulls<String>(foundItems.size)

                    var i = 0
                    foundItems.forEach { value ->
                        items[i] = value
                        i++
                    }

                    val adapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item ,items)
                    stationSpinner.adapter = adapter

                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents: ", exception)
                }
        }
    }

    override fun onResume() {
        if (startDrive) {
            lineInput.visibility = View.VISIBLE
            insertBtn.text = "Fahrt starten!"
        } else {
            lineInput.visibility = View.GONE
            insertBtn.text = "Fahrt beenden!"
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
        super.onResume()
    }

    override fun onPause() {
        if (startDrive) {
            lineInput.visibility = View.VISIBLE
            insertBtn.text = "Fahrt starten!"
        } else {
            lineInput.visibility = View.GONE
            insertBtn.text = "Fahrt beenden!"
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
        super.onPause()
    }

    private fun sendCommandToService(action: String) =
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context?.startService(it)
        }

    private fun addHistory() {
        sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        currentLocation = (activity?.application as MyApplication).getCurrentLocation()

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val uid = user.uid

            val db = Firebase.firestore

            val start = startStation
            var success = false
            lateinit var station: Station

            db.collection("stations").document(start)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {
                        success = true
                        station = document.toObject<Station>()!!
                    } else {
                        Toast.makeText(
                            context,
                            "Bitte wählen Sie eine gültige Station!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (success) {
                        val line = lineInput.text.toString()

                        val c = Calendar.getInstance()

                        val year = c.get(Calendar.YEAR)
                        val month = c.get(Calendar.MONTH)
                        val day = c.get(Calendar.DAY_OF_MONTH)

                        val date = "$day-${month+1}-$year"

                        val hour = c.get(Calendar.HOUR_OF_DAY)
                        val minute = c.get(Calendar.MINUTE)
                        val sec = c.get(Calendar.SECOND)

                        val time = "$hour:$minute:$sec"

                        val route = Route(date, time, start, "", line, uid)

                        val checkLatitude = station.latitude?.minus(currentLocation!!.latitude)
                            ?.let { it1 -> abs(it1) }

                        val checkLongitude = station.longitude?.minus(currentLocation!!.longitude)
                            ?.let { it1 -> abs(it1) }

                        if (checkLatitude != null && checkLongitude != null) {
                            if (checkLatitude <= 0.005 && checkLongitude <= 0.005) {
                                db.collection("routes")
                                    .add(route)
                                    .addOnSuccessListener { documentReference ->
                                        Toast.makeText(
                                            context,
                                            "Fahrt gestartet!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        driveId = documentReference.id
                                        insertBtn.text = "Fahrt beenden!"
                                        passData(driveId)
                                        //stationInput.text.clear()
                                        lineInput.text.clear()
                                        lineInput.visibility = View.GONE

                                        val fragment: Fragment = CardDriveFragment.newInstance(
                                            route.date.toString(),
                                            route.start.toString(),
                                            route.time.toString()
                                        )
                                        val transaction =
                                            activity?.supportFragmentManager!!.beginTransaction()
                                        transaction.replace(
                                            R.id.fragmentContainer,
                                            fragment,
                                            "fragmentTag"
                                        )
                                        transaction.commit()

                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(ContentValues.TAG, "Error adding document", e)

                                    }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Zu weit entfernt von der ausgewählten Station!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }

    private fun editHistory() {

        val end = startStation

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = Firebase.firestore

            var success = false
            lateinit var station: Station

            db.collection("stations").document(end)
                .get()
                .addOnSuccessListener { document ->

                    if (document.exists()) {
                        success = true
                        station = document.toObject<Station>()!!
                    } else {
                        Toast.makeText(
                            context,
                            "Bitte wählen Sie eine gültige Station!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (success) {

                        val checkLatitude = station.latitude?.minus(currentLocation!!.latitude)
                            ?.let { it1 -> abs(it1) }

                        val checkLongitude = station.longitude?.minus(currentLocation!!.longitude)
                            ?.let { it1 -> abs(it1) }

                        if (checkLatitude != null && checkLongitude != null) {
                            if (checkLatitude <= 0.005 && checkLongitude <= 0.005) {

                                Toast.makeText(
                                    context,
                                    "Erfolgreich beendet!",
                                    Toast.LENGTH_LONG
                                ).show()

                                db.collection("routes")
                                    .document(driveId).update("end", end)

                                sendCommandToService(Constants.ACTION_STOP_SERVICE)

                                insertBtn.text = "Fahrt starten!"
                                (requireActivity().application as MyApplication).setStartDrive(true)
                                (requireActivity().application as MyApplication).setDriveId("null")
                                //stationInput.text.clear()
                                lineInput.visibility = View.VISIBLE
                                val fragment: Fragment = HomeFragment()
                                val transaction =
                                    activity?.supportFragmentManager!!.beginTransaction()
                                transaction.replace(R.id.fragmentContainer, fragment, "fragmentTag")
                                transaction.commit()

                            } else {
                                Toast.makeText(
                                    context,
                                    "Zu weit entfernt von der ausgewählten Station!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }


}
