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

class CardFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_card, container, false)

        val tvDep = view.findViewById<TextView>(R.id.tvDep)
        val tvDes = view.findViewById<TextView>(R.id.tvDes)
        tvDep.text = departure
        tvDes.text = destination

        return view

    }

    companion object {
        @JvmStatic
        fun newInstance(departure: String, destination: String) =
            CardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DEP, departure)
                    putString(ARG_DES, destination)
                }
            }
    }
}