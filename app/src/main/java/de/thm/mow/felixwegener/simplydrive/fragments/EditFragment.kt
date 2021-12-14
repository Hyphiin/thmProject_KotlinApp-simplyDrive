package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.MyApplication
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import kotlinx.android.synthetic.main.fragment_edit.view.*
import java.util.*

class EditFragment : Fragment() {

    lateinit var stationInput: EditText
    lateinit var lineInput: EditText
    lateinit var insertBtn: Button

    private lateinit var contextF: FragmentActivity
    private var startDrive: Boolean = true
    private lateinit var driveId: String

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        insertBtn = view.findViewById(R.id.btnInsert)

        val activity = requireActivity()
        contextF = activity
        driveId = (activity.application as MyApplication).getDriveId()!!
        startDrive = (activity.application as MyApplication).getStartDrive()!!

        insertBtn.setOnClickListener { view ->
            //onSearchClick()
            if (startDrive) {
                addHistory()
                (requireActivity().application as MyApplication).setStartDrive(false)
            } else {
                insertBtn.text = "Fahrt beenden!"
                editHistory()
                (requireActivity().application as MyApplication).setStartDrive(true)
                (requireActivity().application as MyApplication).setDriveId("null")
                stationInput.text.clear()
                lineInput.text.clear()
            }
        }

        view.clearHistory.setOnClickListener { view ->
            clearDB()
        }

        return view
    }


    /*private fun onSearchClick() {
        stationInput = view?.findViewById(R.id.stationInput) as EditText
        destinationInput = view?.findViewById(R.id.destinationInput) as EditText

        val departure = stationInput.text.toString()
        val destination = destinationInput.text.toString()

        val fragment: Fragment = CardFragment.newInstance(departure, destination)
        val transaction = activity?.supportFragmentManager!!.beginTransaction()
//transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

     */

    override fun onResume() {
        if (startDrive) {
            insertBtn.text = "Fahrt starten!"
        } else {
            insertBtn.text = "Fahrt beenden!"
        }
        super.onResume()
    }

    override fun onPause() {
        if (startDrive) {
            insertBtn.text = "Fahrt starten!"
        } else {
            insertBtn.text = "Fahrt beenden!"
        }
        super.onPause()
    }

    private fun addHistory() {

        stationInput = view?.findViewById(R.id.stationInput) as EditText
        lineInput = view?.findViewById(R.id.lineInput) as EditText

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val uid = user.uid

            val db = Firebase.firestore

            val start = stationInput.text.toString()
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

                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)

                }
        }
    }

    private fun editHistory() {
        stationInput = view?.findViewById(R.id.stationInput) as EditText
        lineInput = view?.findViewById(R.id.lineInput) as EditText

        val end = stationInput.text.toString()

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = Firebase.firestore

            db.collection("routes")
                .document(driveId).update("end", end)

            insertBtn.text = "Fahrt starten!"
        }
    }

    private fun clearDB() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user!!.uid

        Log.d("TAG", "clearDB")
        val db = Firebase.firestore

        db.collection("routes").whereEqualTo("uid", uid).get().addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }
        }
    }


}