package de.thm.mow.felixwegener.simplydrive


import android.content.ContentValues.TAG

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LatHisAdapter(private val routesList: MutableList<Route>) : RecyclerView.Adapter<LatHisAdapter.LatHisViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatHisViewHolder {

        Log.d("::::::::::::::::::>", parent.context.toString())

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_latest_history, parent, false)

        return LatHisViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LatHisViewHolder, position: Int) {

        Log.d("::::::::::::::::::>", routesList.toString())

        val currentItem = routesList[position]

        holder.tvLHdate.text = currentItem.date
        holder.tvLHtime.text = currentItem.time
        holder.tvLHroute.text = currentItem.route
    }

    override fun getItemCount(): Int {
        return routesList.size
    }

    fun addHistory(route: Route) {
        Log.d("TAG","addHistory")
        val db = Firebase.firestore

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
        Log.d("TAG","clearDB")
        val db = Firebase.firestore

        db.collection("routes").get().addOnSuccessListener { result ->
            for (document in result) {
                document.reference.delete()
            }
        }
    }



    class LatHisViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val tvLHdate : TextView = itemView.findViewById(R.id.tvLHdate)
        val tvLHtime : TextView = itemView.findViewById(R.id.tvLHtime)
        val tvLHroute : TextView = itemView.findViewById(R.id.tvLHroute)

    }
}