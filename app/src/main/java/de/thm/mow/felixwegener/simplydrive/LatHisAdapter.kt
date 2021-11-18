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



    /*
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
                    val tempRoute = Route(
                        document.data.getValue("date").toString(),
                        document.data.getValue("time").toString(),
                        document.data.getValue("route").toString())
                    holder.itemView.setOnClickListener{
                        clickListener.onItemClick(tempRoute)
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



    interface ClickListener {
        fun onItemClick (route: Route)
    }

    override fun getItemCount(): Int {
        return routes.size
    }

    class LatHisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLHdate : TextView = itemView.findViewById(R.id.tvLHdate)
        val tvLHtime : TextView = itemView.findViewById(R.id.tvLHtime)
        val tvLHroute : TextView = itemView.findViewById(R.id.tvLHroute)
    }
    */
}