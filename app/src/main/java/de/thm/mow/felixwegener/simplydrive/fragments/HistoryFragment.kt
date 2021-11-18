package de.thm.mow.felixwegener.simplydrive.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.DocumentChange.*
import de.thm.mow.felixwegener.simplydrive.LatHisAdapter
import de.thm.mow.felixwegener.simplydrive.MainActivity
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import kotlinx.android.synthetic.main.fragment_history.view.*


class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String


    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var routeInput: EditText


    private lateinit var historyView: View
    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var rvLatestHistory: RecyclerView
    private lateinit var routesArrayList: MutableList<Route>
    private lateinit var routesAdapter: LatHisAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("TAG", "##########OnCreateView")

        historyView = inflater.inflate(R.layout.fragment_history, container, false)

        rvLatestHistory = historyView.findViewById(R.id.rvLatestHistory)
        rvLatestHistory.layoutManager = LinearLayoutManager(activity)
        rvLatestHistory.setHasFixedSize(true)

        routesArrayList = mutableListOf<Route>()
        /*
        val r = Route("A", "B", "C", "nv2LTvnPC3YmmxGdNTEGi80phdJ2")
        routesArrayList.add(r)
        */

        routesAdapter = LatHisAdapter(routesArrayList)

        getUserData()

        mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

        if (firebaseUser != null) {
            currentUserID = firebaseUser.uid
        }


       historyView.bShowHistory.setOnClickListener {
            addDBEntry()
        }

        historyView.clearHistory.setOnClickListener {
            clearDB()
        }


        return historyView
    }

    private fun getUserData() {
        Log.d("TAG", "##########getUserData")
        databaseRef = FirebaseFirestore.getInstance()

        databaseRef.collection("routes").whereEqualTo("uid", "nv2LTvnPC3YmmxGdNTEGi80phdJ2")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!){
                        if (dc.type == Type.ADDED) {
                            routesArrayList.add(dc.document.toObject(Route::class.java))
                        }
                    }

                    Log.d("==============>", routesArrayList.toString())

                    routesAdapter = LatHisAdapter(routesArrayList)

                    //historyView.setAdapter(routesAdapter)
                    routesAdapter.notifyDataSetChanged()
                }
            })

    }

    override fun onStart() {
        super.onStart()

    }



    private fun addDBEntry() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val uid = user.uid

            dateInput = view?.findViewById(R.id.DateInput) as EditText
            timeInput = view?.findViewById(R.id.TimeInput) as EditText
            routeInput = view?.findViewById(R.id.RouteInput) as EditText

            val date = dateInput.text.toString()
            val time = timeInput.text.toString()
            val route = routeInput.text.toString()

            val newRoute = Route(date, time, route, uid)
            routesAdapter.addHistory(newRoute)
        }
    }


    private fun clearDB() {
        routesAdapter.clearDB()
    }

    /*override fun onStart() {
        super.onStart()
        Log.d("TAG", "##########OnStart")

        val options =
            FirebaseRecyclerOptions.Builder<Route>().setQuery(databaseRef, Route::class.java)
                .build()

        adapter = object : FirebaseRecyclerAdapter<Route?, RouteViewHolder?>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RouteViewHolder {
                Log.d("TAG", "##########OnCreateViewHolder")
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_latest_history, parent, false)
                return RouteViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: RouteViewHolder,
                position: Int,
                model: Route
            ) {
                Log.d("TAG", "##########ViewHolder")
                databaseRef.child(currentUserID)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val date = dataSnapshot.child("date").value.toString()
                            val time = dataSnapshot.child("time").value.toString()
                            val route = dataSnapshot.child("route").value.toString()

                            holder.tvLHdate.text = date
                            holder.tvLHtime.text = time
                            holder.tvLHroute.text = route

                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Failed to read value
                            Toast.makeText(
                                activity,
                                "Fail!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }


        }



        rvLatestHistory.adapter = adapter
        adapter.startListening()
    }

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvLHdate = itemView.findViewById(R.id.tvLHdate) as TextView
        val tvLHtime = itemView.findViewById(R.id.tvLHtime) as TextView
        val tvLHroute = itemView.findViewById(R.id.tvLHroute) as TextView

        fun RoutesViewHolder(itemView: View) {
            super.itemView
        }
    }*/



    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("TAG", "##########OnViewCreated")

    }

    private fun initRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvLatestHistory)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val uid = user.uid
        val routes = getHistoryEntries(uid)
        recyclerView.setHasFixedSize(true)

        latHisAdapter = LatHisAdapter(routes, this) }
        recyclerView.adapter = latHisAdapter

    }

    private fun getHistoryEntries (uid: String): MutableList<Route> {
        val rTable = mutableListOf<Route>()
        val db = Firebase.firestore

        db.collection("routes")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(ContentValues.TAG, "${document.id} => ${document.data}")
                    val r = Route(document.data.getValue("date").toString(), document.data.getValue("time").toString(), document.data.getValue("route").toString(), uid)
                    rTable.add(r)

                }
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
        return rTable
    }*/




    /*
    override fun onItemClick(route: Route) {
       val fragment: Fragment = HistoryDetailView.newInstance(route.route!!)
       val transaction = activity?.supportFragmentManager!!.beginTransaction()
       //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
       transaction.replace(R.id.fragmentContainer, fragment)
       transaction.addToBackStack(null)
       transaction.commit()

    }

     */
}







