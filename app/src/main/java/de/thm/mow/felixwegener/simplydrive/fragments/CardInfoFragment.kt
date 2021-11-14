package de.thm.mow.felixwegener.simplydrive.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.thm.mow.felixwegener.simplydrive.R

private const val ARG_DEP = "departure"
private const val ARG_DES = "destination"

class CardInfoFragment : Fragment() {
    private var departure: String? = null
    private var destination: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            departure = it.getString(ARG_DEP)
            destination = it.getString(ARG_DES)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_card_info, container, false)

        val tvDepInfo = view.findViewById<TextView>(R.id.tvDepInfo)
        val tvDesInfo = view.findViewById<TextView>(R.id.tvDesInfo)
        tvDepInfo.text = departure
        tvDesInfo.text = destination

        return view

    }

    companion object {
        @JvmStatic
        fun newInstance(departure: String, destination: String) =
            CardInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEP, departure)
                    putString(ARG_DES, destination)
                }
            }
    }
}