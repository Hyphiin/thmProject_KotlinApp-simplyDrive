    package de.thm.mow.felixwegener.simplydrive.fragments

    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.EditText
    import android.widget.TextView
    import android.widget.Toast
    import androidx.fragment.app.Fragment
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.firebase.ui.database.FirebaseRecyclerAdapter
    import com.firebase.ui.database.FirebaseRecyclerOptions
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.*
    import de.thm.mow.felixwegener.simplydrive.LatHisAdapter
    import de.thm.mow.felixwegener.simplydrive.R
    import de.thm.mow.felixwegener.simplydrive.Route
    import kotlinx.android.synthetic.main.fragment_history.*


    class HistoryFragment : Fragment(R.layout.fragment_history), LatHisAdapter.ClickListener {

        private lateinit var latHisAdapter: LatHisAdapter

        lateinit var dateInput: EditText
        lateinit var timeInput: EditText
        lateinit var routeInput: EditText
        private lateinit var newRoute: Route

        private lateinit var databaseRef: DatabaseReference
        private lateinit var usersRef: DatabaseReference
        private lateinit var mAuth: FirebaseAuth
        private lateinit var currentUserID: String

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            /*val view = inflater.inflate(R.layout.fragment_history, container, false)
            initRecyclerView(view)


            view.bShowHistory.setOnClickListener { view ->
                addDBEntry()
            }
            view.clearHistory.setOnClickListener { view ->
                clearDB()
            }*/

            val view = inflater.inflate(R.layout.fragment_history, container, false)
            rvLatestHistory.layoutManager = LinearLayoutManager(activity)

            mAuth = FirebaseAuth.getInstance()
            currentUserID = mAuth.currentUser?.uid ?: "Moin"

            databaseRef = FirebaseDatabase.getInstance().reference.child("routes")

            return view
        }

        override fun onStart() {
            super.onStart()

            val options = FirebaseRecyclerOptions.Builder<Route>().setQuery(databaseRef, Route::class.java).build()

            val adapter: FirebaseRecyclerAdapter<Route?, RouteViewHolder?> =
                object : FirebaseRecyclerAdapter<Route?, RouteViewHolder?>(options) {
                    override fun onBindViewHolder(
                        holder: RouteViewHolder,
                        position: Int,
                        model: Route
                    ) {
                        val user = FirebaseAuth.getInstance().currentUser
                        user?.let {
                            val uid = user.uid

                            databaseRef.child(uid).addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot){
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

                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
                        // Create a new instance of the ViewHolder, in this case we are using a custom
                        // layout called R.layout.message for each item
                        val view: View = LayoutInflater.from(parent.context)
                            .inflate(R.layout.item_latest_history, parent, false)
                        return RouteViewHolder(view)
                    }
                }

            rvLatestHistory.adapter = adapter
            adapter.startListening()
        }

        public class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val tvLHdate = itemView.findViewById(R.id.tvLHdate) as TextView
            val tvLHtime = itemView.findViewById(R.id.tvLHtime) as TextView
            val tvLHroute = itemView.findViewById(R.id.tvLHroute) as TextView

            public fun RoutesViewHolder(itemView: View){
                super.itemView
            }
        }




        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            rvLatestHistory.apply {
                layoutManager = LinearLayoutManager (activity)
                adapter = latHisAdapter
            }
        }



        /*private fun initRecyclerView(view: View) {
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

            newRoute = Route(date, time, route, uid)
            latHisAdapter.addHistory(newRoute)}

        }

        private fun clearDB() {
            latHisAdapter.clearDB()
        }*/

        override fun onItemClick(route: Route) {
           val fragment: Fragment = HistoryDetailView.newInstance(route.route!!)
           val transaction = activity?.supportFragmentManager!!.beginTransaction()
           //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
           transaction.replace(R.id.fragmentContainer, fragment)
           transaction.addToBackStack(null)
           transaction.commit()

        }

    }