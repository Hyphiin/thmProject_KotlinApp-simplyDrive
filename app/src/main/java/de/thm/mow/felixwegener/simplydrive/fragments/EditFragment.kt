package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.MyApplication
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import de.thm.mow.felixwegener.simplydrive.Station
import kotlinx.android.synthetic.main.fragment_edit.view.*
import java.util.*
import kotlin.math.abs

class EditFragment : Fragment() {

    lateinit var stationInput: EditText
    lateinit var lineInput: EditText
    lateinit var insertBtn: Button

    private lateinit var contextF: FragmentActivity
    private var startDrive: Boolean = true
    private lateinit var driveId: String

    private var currentLocation: Location? = null
    private var endDrive: Boolean? = false

    lateinit var dataPasser: OnDataPass

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
        stationInput = view.findViewById(R.id.stationInput)
        lineInput = view.findViewById(R.id.lineInput)

        val activity = requireActivity()
        contextF = activity
        driveId = (activity.application as MyApplication).getDriveId()!!
        startDrive = (activity.application as MyApplication).getStartDrive()!!

        insertBtn.setOnClickListener { view ->
            if (startDrive) {
                addHistory()
                (requireActivity().application as MyApplication).setStartDrive(false)
            } else {
                insertBtn.text = "Fahrt beenden!"
                editHistory()
                if (endDrive == true) {
                    (requireActivity().application as MyApplication).setStartDrive(true)
                    (requireActivity().application as MyApplication).setDriveId("null")
                    stationInput.text.clear()
                    lineInput.visibility = View.VISIBLE
                }
            }
        }

        return view
    }

    override fun onResume() {
        if (startDrive) {
            lineInput.visibility = View.VISIBLE
            insertBtn.text = "Fahrt starten!"
        } else {
            lineInput.visibility = View.GONE
            insertBtn.text = "Fahrt beenden!"
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
        }
        super.onPause()
    }

    private fun addHistory() {

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val uid = user.uid

            val db = Firebase.firestore

            val start = stationInput.text.toString()
            var success = false
            lateinit var station: Station

            db.collection("stations").document(start)
                .get()
                .addOnSuccessListener { document ->
                    Log.d("___________", document.toString())

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

                        val date = "$day-$month-$year"

                        val hour = c.get(Calendar.HOUR_OF_DAY)
                        val minute = c.get(Calendar.MINUTE)
                        val sec = c.get(Calendar.SECOND)

                        val time = "$hour:$minute:$sec"

                        val route = Route(date, time, start, "", line, uid)

                        val checkLatitude = station.latitude?.minus(currentLocation!!.latitude)
                            ?.let { it1 -> abs(it1) }

                        val checkLongitude = station.longitude?.minus(currentLocation!!.longitude)
                            ?.let { it1 -> abs(it1) }

                        Toast.makeText(
                            context,
                            " Lat: $checkLatitude, Long: $checkLongitude",
                            Toast.LENGTH_LONG
                        ).show()

                        if (checkLatitude != null && checkLongitude != null) {
                            if (checkLatitude <= 0.005 && checkLongitude <= 0.005) {
                                Toast.makeText(
                                    context,
                                    "Nah genug!",
                                    Toast.LENGTH_LONG
                                ).show()
                                db.collection("routes")
                                    .add(route)
                                    .addOnSuccessListener { documentReference ->
                                        Log.d(
                                            ContentValues.TAG,
                                            "DocumentSnapshot added with ID: ${documentReference.id}"
                                        )
                                        driveId = documentReference.id
                                        insertBtn.text = "Fahrt beenden!"
                                        passData(driveId)
                                        stationInput.text.clear()
                                        lineInput.text.clear()
                                        lineInput.visibility = View.GONE

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

        val end = stationInput.text.toString()

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = Firebase.firestore

            var success = false
            lateinit var station: Station

            db.collection("stations").document(end)
                .get()
                .addOnSuccessListener { document ->
                    Log.d("___________", document.toString())

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

                        Toast.makeText(
                            context,
                            " Lat: $checkLatitude, Long: $checkLongitude",
                            Toast.LENGTH_LONG
                        ).show()

                        if (checkLatitude != null && checkLongitude != null) {
                            if (checkLatitude <= 0.005 && checkLongitude <= 0.005) {
                                Toast.makeText(
                                    context,
                                    "Nah genug!",
                                    Toast.LENGTH_LONG
                                ).show()

                                db.collection("routes")
                                    .document(driveId).update("end", end)

                                insertBtn.text = "Fahrt starten!"

                                endDrive = true
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