package de.thm.mow.felixwegener.simplydrive.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import de.thm.mow.felixwegener.simplydrive.LatHisAdapter
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.Route
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String

    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var routesArrayList: MutableList<Route>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.cvHomeOne.setOnClickListener { view ->
            onHomeCardClicked()
        }
        view.cvHomeTwo.setOnClickListener { view ->
            onHomeCardClicked()
        }

        view.cvHomeThree.setOnClickListener { view ->
            onHomeCardClicked()
        }

        return view
    }

    private fun onHomeCardClicked(){
        val fragment: Fragment = CardInfoFragment.newInstance("Hanau HBF - ", "Wetzlar Bahnhof")
        val transaction = activity?.supportFragmentManager!!.beginTransaction()
        //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun getUserData(): MutableList<Route> {
        routesArrayList = mutableListOf()
        databaseRef = FirebaseFirestore.getInstance()

        mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

        if (firebaseUser != null) {
            currentUserID = firebaseUser.uid
        }

        databaseRef.collection("routes").whereEqualTo("uid", currentUserID).orderBy("date", Query.Direction.DESCENDING).limit(3)
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

                }
            })

        return routesArrayList

    }

}