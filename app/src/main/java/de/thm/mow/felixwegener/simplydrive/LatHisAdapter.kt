package de.thm.mow.felixwegener.simplydrive


import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatHisViewHolder {

        Log.d("::::::::::::::::::>", parent.context.toString())

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_latest_history, parent, false)

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

        /*return LatHisViewHolder(itemView).listen { pos, _ ->
            val item = routesList[pos]
            //TODO do other stuff here
            Log.d("HEEEEEELLLLLLLOOOO", item.toString())

            tvDatePopUp.text = item.date.toString()
            tvTimePopUp.text = item.time
            tvDeparturePopUp.text = item.start
            tvDestinationPopUp.text = item.end
            tvLinePopUp.text = item.line

            myDialog.show()
        }*/

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