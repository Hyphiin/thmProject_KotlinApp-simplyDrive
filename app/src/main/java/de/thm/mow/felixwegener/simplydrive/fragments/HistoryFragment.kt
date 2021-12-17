package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.DocumentChange.*
import de.thm.mow.felixwegener.simplydrive.LatHisAdapter
import de.thm.mow.felixwegener.simplydrive.Location
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import java.io.IOException
import java.io.OutputStreamWriter


class HistoryFragment : Fragment(R.layout.fragment_history), LatHisAdapter.ClickListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String

    private lateinit var historyView: View
    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var rvLatestHistory: RecyclerView
    private lateinit var routesArrayList: MutableList<Route>
    private lateinit var routesAdapter: LatHisAdapter

    private lateinit var routePoints: MutableList<Location>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUserData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        historyView = inflater.inflate(R.layout.fragment_history, container, false)

        rvLatestHistory = historyView.findViewById(R.id.rvLatestHistory)
        rvLatestHistory.layoutManager = LinearLayoutManager(activity)
        rvLatestHistory.setHasFixedSize(true)

        routesArrayList = mutableListOf<Route>()

        return historyView
    }

    private fun getUserData() {
        databaseRef = FirebaseFirestore.getInstance()

        mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

        if (firebaseUser != null) {
            currentUserID = firebaseUser.uid
        }

        databaseRef.collection("routes").whereEqualTo("uid", currentUserID)
            .orderBy("date", Query.Direction.DESCENDING).orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == Type.ADDED) {
                            routesArrayList.add(dc.document.toObject(Route::class.java))
                        }
                    }

                    routesAdapter = LatHisAdapter(routesArrayList, this@HistoryFragment)
                    rvLatestHistory.adapter = routesAdapter

                    routesAdapter.notifyDataSetChanged()

                }
            })

    }

    override fun onItemClick(route: Route) {
        //ToDo
        val time = route.time
        val date = route.date
        var currentRoute: String
        routePoints = mutableListOf()

        databaseRef = FirebaseFirestore.getInstance()

        mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

        if (firebaseUser != null) {
            currentUserID = firebaseUser.uid
        }

        val foundItems = mutableListOf<String>()

        databaseRef.collection("routes").whereEqualTo("uid", currentUserID)
            .whereEqualTo("time", time.toString()).whereEqualTo("date", date.toString())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                    foundItems.add(document.id)
                }

                Log.d("----------->", foundItems.toString())

                currentRoute = foundItems.first()

                databaseRef.collection("locations")
                    .whereEqualTo("uid", currentUserID)
                    .whereEqualTo("routeId", currentRoute)
                    .get()
                    .addOnSuccessListener { points ->
                        val length = points.size()
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
                            } catch (e: IOException) {
                                Log.e("Exception", "File write failed: $e")
                            }

                            val firstLoc = routePoints.first()?.location?.locations
                            val lonArray = DoubleArray(routePoints.size)
                            val latArray = DoubleArray(routePoints.size)

                            var idx = 0
                            routePoints.forEach { entry ->
                                val tempLon = idx
                                val tempLat = idx

                                if (entry != null) {
                                    lonArray[tempLon] =
                                        entry.location?.locations?.longitude!!
                                }
                                if (entry != null) {
                                    latArray[tempLat] =
                                        entry.location?.locations?.latitude!!
                                }

                                idx++
                            }

                            // die gefundene Route
                            val fragment: Fragment = CardInfoFragment.newInstance(
                                route.start.toString(),
                                route.end.toString(),
                                route.time.toString(),
                                firstLoc?.longitude!!,
                                firstLoc?.latitude!!,
                                lonArray,
                                latArray
                            )
                            val transaction =
                                activity?.supportFragmentManager!!.beginTransaction()
                            //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("fragmentTag")!!)
                            transaction.add(R.id.fragmentContainer, fragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
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







