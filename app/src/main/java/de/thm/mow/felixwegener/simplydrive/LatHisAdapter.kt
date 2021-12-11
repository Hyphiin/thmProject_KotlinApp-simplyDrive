package de.thm.mow.felixwegener.simplydrive


import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.lang.Exception

import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot


class LatHisAdapter(private val routesList: MutableList<Route>, private val clickListener: ClickListener) :
    RecyclerView.Adapter<LatHisAdapter.LatHisViewHolder>() {

    private lateinit var tvDatePopUp: TextView
    private lateinit var tvTimePopUp: TextView
    private lateinit var tvDeparturePopUp: TextView
    private lateinit var tvDestinationPopUp: TextView
    private lateinit var tvLinePopUp: TextView
    private lateinit var showMapBtn: Button
    private lateinit var exportBtn: Button

    private lateinit var databaseRef: FirebaseFirestore
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String
    private lateinit var currentRoute: String

    private lateinit var routePoints: MutableList<Location?>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatHisViewHolder {

        Log.d("::::::::::::::::::>", parent.context.toString())

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_latest_history, parent, false)

        routePoints = mutableListOf()

        val myDialog = Dialog(parent.context)
        myDialog.setContentView(R.layout.dialog__drive)

        tvDatePopUp = myDialog.findViewById(R.id.tvLHdatePopUp)
        tvTimePopUp = myDialog.findViewById(R.id.tvLHtimePopUp)
        tvDeparturePopUp = myDialog.findViewById(R.id.tvLHrouteStartPopUp)
        tvDestinationPopUp = myDialog.findViewById(R.id.tvLHrouteEndPopUp)
        tvLinePopUp = myDialog.findViewById(R.id.tvLHrouteLinePopUp)
        showMapBtn = myDialog.findViewById(R.id.btn__showMap)
        exportBtn = myDialog.findViewById(R.id.btn__export)



        exportBtn.setOnClickListener {
            Toast.makeText(parent.context, "Export!", Toast.LENGTH_SHORT).show()
        }

        return LatHisViewHolder(itemView)

        return LatHisViewHolder(itemView).listen { pos, _ ->
            val item = routesList[pos]
            //TODO do other stuff here
            Log.d("HEEEEEELLLLLLLOOOO", item.toString())

            var outputStream: FileOutputStream

            val time = routesList[pos].time
            val date = routesList[pos].date

            databaseRef = FirebaseFirestore.getInstance()

            mAuth = FirebaseAuth.getInstance()
            val firebaseUser = mAuth.currentUser

            if (firebaseUser != null) {
                currentUserID = firebaseUser.uid
            }

            databaseRef.collection("routes").whereEqualTo("uid", currentUserID).whereEqualTo("time", time).whereEqualTo("date", date.toString())
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        currentRoute = document.id
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

            currentRoute = "ypW2olQp1kasQxnVbFOY"

            databaseRef.collection("locations").whereEqualTo("uid", currentUserID)
                .get()
                .addOnSuccessListener { points ->
                    val length = points.size()
                    Log.d("....................", length.toString())
                    for (point in points) {
                        Log.d(TAG, "$point => $point")
                        routePoints.add(point.toObject(Location::class.java))
                    }

                    Log.d("-------------------->", routePoints.toString())
                    if (routePoints.isNotEmpty()) {

                        val array: Array<Location> = routePoints.stream().toArray { arrayOfNulls<Location>(it) }

                        val array2 = arrayOf(routePoints.first())

                        Log.d("array---------->", array2.toString())

                        try {
                            outputStream = parent.context.openFileOutput("${currentRoute}.txt", MODE_PRIVATE)
                            /*for (item in array) {
                                outputStream.write(array.toByteArray())
                            }*/
                            outputStream.write(array2.toByteArray())
                            Log.d("Saved to: ", parent.context.filesDir.toString())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }


            tvDatePopUp.text = routesList[pos].date.toString()
            tvTimePopUp.text = routesList[pos].time
            tvDeparturePopUp.text = routesList[pos].start
            tvDestinationPopUp.text = routesList[pos].end
            tvLinePopUp.text = routesList[pos].line

            myDialog.show()
        }

    }

    override fun onBindViewHolder(holder: LatHisViewHolder, position: Int) {

        Log.d("::::::::::::::::::>", "${routesList[position]} , $position")

        val currentItem = routesList[position]

        holder.tvLHdate.text = currentItem.date.toString()
        holder.tvLHtime.text = currentItem.time
        holder.tvLHrouteStart.text = currentItem.start
        holder.tvlHrouteEnd.text = currentItem.end
        holder.tvlHrouteLine.text = currentItem.line

        holder.itemView.setOnClickListener{
            clickListener.onItemClick(routesList[position])
        }
    }

    override fun getItemCount(): Int {
        return routesList.size
    }

    class LatHisViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvLHdate: TextView = itemView.findViewById(R.id.tvLHdate)
        val tvLHtime: TextView = itemView.findViewById(R.id.tvLHtime)
        val tvLHrouteStart: TextView = itemView.findViewById(R.id.tvLHrouteStart)
        val tvlHrouteEnd: TextView = itemView.findViewById(R.id.tvLHrouteEnd)
        val tvlHrouteLine: TextView = itemView.findViewById(R.id.tvLHrouteLine)

    }

    fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(getAdapterPosition(), itemViewType)
        }
        return this
    }

    interface ClickListener {
        fun onItemClick(route: Route)
    }
}

private fun <T> Array<T>.toByteArray(): ByteArray? {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream: ObjectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(this)
    objectOutputStream.flush()
    val result = byteArrayOutputStream.toByteArray()
    byteArrayOutputStream.close()
    objectOutputStream.close()
    return result
}
