package de.thm.mow.felixwegener.simplydrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment1.*

class Fragment1 :Fragment(R.layout.fragment1) {

    lateinit var latHisAdapter: LatHisAdapter

    lateinit var dateInput: EditText
    lateinit var timeInput: EditText
    lateinit var routeInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        latHisAdapter = LatHisAdapter(mutableListOf())

        rvLatestHistory.adapter = latHisAdapter
        rvLatestHistory.layoutManager = LinearLayoutManager(this.context)


        bShowHistory.setOnClickListener {
            addDBEntry()
        }

        clearHistory.setOnClickListener {
            clearDB()
        }
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

}