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

        historyView.bShowHistory.setOnClickListener {
        addDBEntry()
        }

        historyView.clearHistory.setOnClickListener {
        clearDB()
        }


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
            Log.d("==========>", routesArrayList.toString())

        routesAdapter = LatHisAdapter(routesArrayList)
            rvLatestHistory.adapter = routesAdapter

        routesAdapter.notifyDataSetChanged()

        }
        })

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


        }







