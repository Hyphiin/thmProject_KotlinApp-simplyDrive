package de.thm.mow.felixwegener.simplydrive

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class LatHisAdapter(
    private val routesList: MutableList<Route>,
    private val clickListener: ClickListener
) :
    RecyclerView.Adapter<LatHisAdapter.LatHisViewHolder>() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatHisViewHolder {

        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_latest_history, parent, false)

        return LatHisViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: LatHisViewHolder, position: Int) {

        val currentItem = routesList[position]

        holder.tvLHdate.text = currentItem.date.toString()
        holder.tvLHtime.text = currentItem.time
        holder.tvLHrouteStart.text = currentItem.start
        holder.tvlHrouteEnd.text = currentItem.end
        holder.tvlHrouteLine.text = currentItem.line

        holder.itemView.setOnClickListener {
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

    interface ClickListener {
        fun onItemClick(route: Route)
    }
}
