package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import de.thm.mow.felixwegener.simplydrive.*
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.services.TrackingService


class HomeFragment : Fragment(), LatHisAdapter.ClickListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String

    private var startDrive: Boolean = false

    private lateinit var homeView: View
    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var rvLatestHistory: RecyclerView
    private lateinit var routesArrayList: MutableList<Route>
    private lateinit var routesAdapter: LatHisAdapter
    private lateinit var sorryTv: TextView
    private lateinit var logoContainer: CardView

    private lateinit var routePoints: MutableList<Location>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUserData()
        sendCommandToService(Constants.ACTION_STOP_SERVICE)
        startDrive = (requireActivity().application as MyApplication).getStartDrive()!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeView = inflater.inflate(R.layout.fragment_home, container, false)

        rvLatestHistory = homeView.findViewById(R.id.rvLatestHistory)
        rvLatestHistory.layoutManager = LinearLayoutManager(activity)
        rvLatestHistory.setHasFixedSize(true)
        sorryTv = homeView.findViewById(R.id.sorry__tv)
        logoContainer = homeView.findViewById(R.id.logoContainer)

        routesArrayList = mutableListOf<Route>()

        return homeView
    }

    private fun sendCommandToService(action: String) =
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context?.startService(it)
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
            .limit(3)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            routesArrayList.add(dc.document.toObject(Route::class.java))
                        }
                    }

                    if (routesArrayList.size > 0) {
                        rvLatestHistory.visibility = View.VISIBLE
                        sorryTv.visibility = View.GONE
                        logoContainer.visibility = View.GONE
                        routesAdapter = LatHisAdapter(routesArrayList, this@HomeFragment)
                        rvLatestHistory.adapter = routesAdapter

                        routesAdapter.notifyDataSetChanged()
                    } else {
                        rvLatestHistory.visibility = View.GONE
                        sorryTv.visibility = View.VISIBLE
                        logoContainer.visibility = View.VISIBLE
                    }

                }
            })

    }

    override fun onItemClick(route: Route) {
        //ToDo
        val time = route.time
        val date = route.date
        val start = route.start
        val line = route.line
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
                    foundItems.add(document.id)
                }

                currentRoute = foundItems.first()

                databaseRef.collection("locations")
                    .whereEqualTo("uid", currentUserID)
                    .whereEqualTo("routeId", currentRoute)
                    .get()
                    .addOnSuccessListener { points ->
                        for (point in points) {
                            routePoints.add(point.toObject(Location::class.java))
                        }
                        routePoints.sortBy { location: Location -> location.location?.lastLocation?.time }

                        if (routePoints.isNotEmpty()) {

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
                                route.date.toString(),
                                route.start.toString(),
                                route.end.toString(),
                                route.time.toString(),
                                firstLoc?.longitude!!,
                                firstLoc?.latitude!!,
                                currentUserID,
                                time!!
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