package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import de.thm.mow.felixwegener.simplydrive.LatHisAdapter
import de.thm.mow.felixwegener.simplydrive.Location
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route


class HomeFragment : Fragment(), LatHisAdapter.ClickListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String

    private lateinit var homeView: View
    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var rvLatestHistory: RecyclerView
    private lateinit var routesArrayList: MutableList<Route>
    private lateinit var routesAdapter: LatHisAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getUserData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeView = inflater.inflate(R.layout.fragment_home, container, false)

        rvLatestHistory = homeView.findViewById(R.id.rvLatestHistory)
        rvLatestHistory.layoutManager = LinearLayoutManager(activity)
        rvLatestHistory.setHasFixedSize(true)

        routesArrayList = mutableListOf<Route>()

        return homeView
    }

   /* private fun onHomeCardClicked(){
        val fragment: Fragment = CardInfoFragment.newInstance("Hanau HBF - ", "Wetzlar Bahnhof", )
        val transaction = activity?.supportFragmentManager!!.beginTransaction()
        transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }*/


    private fun getUserData() {
        databaseRef = FirebaseFirestore.getInstance()

        mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

        if (firebaseUser != null) {
            currentUserID = firebaseUser.uid
        }

        databaseRef.collection("routes").whereEqualTo("uid", currentUserID)
            .orderBy("date", Query.Direction.DESCENDING).orderBy("time", Query.Direction.DESCENDING).limit(3)
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED) {
                            routesArrayList.add(dc.document.toObject(Route::class.java))
                        }
                    }
                    Log.d("==========>", routesArrayList.toString())

                    routesAdapter = LatHisAdapter(routesArrayList, this@HomeFragment)
                    rvLatestHistory.adapter = routesAdapter

                    routesAdapter.notifyDataSetChanged()

                }
            })

    }

    override fun onItemClick (route: Route){
        val routePoints = mutableListOf<Location>()

        //routeID einfügen
        databaseRef.collection("locations").whereEqualTo("routeId", "1OqNMPWol0h6EfdXN5o4")
            .get()
            .addOnSuccessListener { points ->
                val length = points.size()
                Log.d("....................", length.toString())
                for (point in points) {
                    Log.d(ContentValues.TAG, "$point => $point")
                    routePoints.add(point.toObject(Location::class.java))
                }


            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }

        val firstLoc = routePoints.first().location?.locations
        val lonArray: DoubleArray = DoubleArray(0)
        val latArray: DoubleArray = DoubleArray(0)

        routePoints.forEach { entry ->
            val tempLon = lonArray.size
            val tempLat = latArray.size

            lonArray[tempLon] = entry.location?.locations?.longitude!!
            lonArray[tempLat] = entry.location?.locations?.latitude!!
        }



        val fragment: Fragment = CardInfoFragment.newInstance(route.start.toString(), route.end.toString(), route.time.toString(), firstLoc?.longitude!!, firstLoc?.latitude!!, lonArray, latArray )
        val transaction = activity?.supportFragmentManager!!.beginTransaction()
        //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("fragmentTag")!!)
        transaction.add(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}