package de.thm.mow.felixwegener.simplydrive

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_latest_history.view.*

class LatHisAdapter (
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
        routes.add(route)
        notifyItemInserted(routes.size - 1)
    }

    override fun onBindViewHolder(holder: LatHisViewHolder, position: Int) {
        val curLatHis = routes[position]
        holder.itemView.apply{
            tvLHdate.text = curLatHis.date
            tvLHtime.text = curLatHis.time
            tvLHroute.text = curLatHis.route
        }
    }

    override fun getItemCount(): Int {
        return routes.size
    }
}