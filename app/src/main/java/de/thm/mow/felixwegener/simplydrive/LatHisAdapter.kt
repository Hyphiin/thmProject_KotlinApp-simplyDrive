package de.thm.mow.felixwegener.simplydrive

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.item_latest_history.view.*

class LatHisAdapter(
    private val routes: MutableList<Route>
) : RecyclerView.Adapter<LatHisAdapter.LatHisViewHolder>() {

    class LatHisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatHisViewHolder {
        return LatHisViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_latest_history,
                parent,
                false
            )
        )
    }

    fun addHistory(route: Route) {
        val db = Firebase.firestore

        routes.add(route)
        notifyItemInserted(routes.size - 1)

        db.collection("routes")
            .add(route)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)

            }
    }

    fun clearDB() {
        val db = Firebase.firestore

        db.collection("routes").get().addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }
        }
    }

    // next step -> just show local routes Array, then on initial load, get all routes of user
    // oder immer nur letztes Item bekommen und anzeigen
    override fun onBindViewHolder(holder: LatHisViewHolder, position: Int) {
        /*
        val db = Firebase.firestore
        db.collection("routes")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")

                    holder.itemView.apply {
                        tvLHdate.text = document.data.getValue("date").toString()
                        tvLHtime.text = document.data.getValue("time").toString()
                        tvLHroute.text = document.data.getValue("route").toString()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        */

        val rTable = routes[position]
        holder.itemView.apply {
            tvLHdate.text = rTable.date
            tvLHtime.text = rTable.time
            tvLHroute.text = rTable.route
        }



    }

    override fun getItemCount(): Int {
        return routes.size
    }
}