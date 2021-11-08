package de.thm.mow.felixwegener.simplydrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.view.*

class HistoryFragment :Fragment(R.layout.fragment_history), LatHisAdapter.ClickListener {

    private lateinit var latHisAdapter: LatHisAdapter

    lateinit var dateInput: EditText
    lateinit var timeInput: EditText
    lateinit var routeInput: EditText

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
        latHisAdapter = LatHisAdapter(mutableListOf(), this)
        recyclerView.adapter = latHisAdapter
    }



    private fun addDBEntry() {

        dateInput = view?.findViewById(R.id.DateInput) as EditText
        timeInput = view?.findViewById(R.id.TimeInput) as EditText
        routeInput = view?.findViewById(R.id.RouteInput) as EditText

        val date = dateInput.text.toString()
        val time = timeInput.text.toString()
        val route = routeInput.text.toString()

        val newRoute = Route(date, time, route)
        latHisAdapter.addHistory(newRoute)
    }

    private fun clearDB() {
        latHisAdapter.clearDB()
    }

    override fun onItemClick(route: Route) {
       val fragment: Fragment = HistoryDetailView.newInstance(route.route!!)
       val transaction = activity?.supportFragmentManager!!.beginTransaction()
       transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("history_fragment")!!)
       transaction.add(R.id.fragmentContainer, fragment)
       transaction.addToBackStack(null)
       transaction.commit()

    }

}