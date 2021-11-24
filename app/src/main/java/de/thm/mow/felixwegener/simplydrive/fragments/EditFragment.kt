package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import kotlinx.android.synthetic.main.fragment_edit.view.*
import java.util.*

class EditFragment : Fragment() {

    lateinit var departureInput: EditText
    lateinit var destinationInput: EditText
    lateinit var lineInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        view.btnInsert.setOnClickListener { view ->
            //onSearchClick()
            addHistory()
        }

        view.clearHistory.setOnClickListener { view ->
            clearDB()
        }

        return view
    }


    /*private fun onSearchClick() {
        departureInput = view?.findViewById(R.id.departureInput) as EditText
        destinationInput = view?.findViewById(R.id.destinationInput) as EditText

        val departure = departureInput.text.toString()
        val destination = destinationInput.text.toString()

        val fragment: Fragment = CardFragment.newInstance(departure, destination)
        val transaction = activity?.supportFragmentManager!!.beginTransaction()
//transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

     */

    private fun addHistory() {

        departureInput = view?.findViewById(R.id.departureInput) as EditText
        destinationInput = view?.findViewById(R.id.destinationInput) as EditText
        lineInput = view?.findViewById(R.id.lineInput) as EditText

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val uid = user.uid

            val db = Firebase.firestore

            val start = departureInput.text.toString()
            val end = destinationInput.text.toString()
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

            val route = Route(date, time, start, end, line, uid)

            db.collection("routes")
                .add(route)
                .addOnSuccessListener { documentReference ->
                    Log.d(
                        ContentValues.TAG,
                        "DocumentSnapshot added with ID: ${documentReference.id}"
                    )

                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)

                }
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