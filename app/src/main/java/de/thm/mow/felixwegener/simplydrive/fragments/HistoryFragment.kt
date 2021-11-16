    package de.thm.mow.felixwegener.simplydrive.fragments

    import android.content.ContentValues
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
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase
    import de.thm.mow.felixwegener.simplydrive.LatHisAdapter
    import de.thm.mow.felixwegener.simplydrive.R
    import de.thm.mow.felixwegener.simplydrive.Route
    import kotlinx.android.synthetic.main.fragment_history.view.*
    import kotlinx.android.synthetic.main.fragment_history_detail_view.view.*
    import kotlinx.android.synthetic.main.item_latest_history.*
    import kotlinx.android.synthetic.main.item_latest_history.view.*

    class HistoryFragment :Fragment(R.layout.fragment_history), LatHisAdapter.ClickListener {

        private lateinit var latHisAdapter: LatHisAdapter

        lateinit var dateInput: EditText
        lateinit var timeInput: EditText
        lateinit var routeInput: EditText
        private lateinit var newRoute: Route

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_history, container, false)
            initRecyclerView(view)

            view.bShowHistory.setOnClickListener { view ->
                addDBEntry()
            }
            view.clearHistory.setOnClickListener { view ->
                clearDB()
            }


            return view
        }



        private fun initRecyclerView(view: View) {
            val recyclerView = view.findViewById<RecyclerView>(R.id.rvLatestHistory)
            recyclerView.layoutManager = LinearLayoutManager(activity)

            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                val uid = user.uid
            val routes = getHistoryEntries(uid)
            latHisAdapter = LatHisAdapter(routes, this)
            recyclerView.adapter = latHisAdapter}
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
        }

        override fun onItemClick(route: Route) {
           val fragment: Fragment = HistoryDetailView.newInstance(route.route!!)
           val transaction = activity?.supportFragmentManager!!.beginTransaction()
           //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
           transaction.replace(R.id.fragmentContainer, fragment)
           transaction.addToBackStack(null)
           transaction.commit()

        }

    }