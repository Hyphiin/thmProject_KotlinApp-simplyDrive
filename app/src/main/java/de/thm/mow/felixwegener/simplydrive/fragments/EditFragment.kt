package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.api.Billing
import de.thm.mow.felixwegener.simplydrive.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_edit.*
import kotlinx.android.synthetic.main.fragment_edit.view.*

class EditFragment : Fragment() {

    lateinit var departureInput: EditText
    lateinit var destinationInput: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit, container, false)

        view.btnSearch.setOnClickListener { view ->
            onSearchClick()
        }

        return view
    }


    private fun onSearchClick(){
        departureInput = view?.findViewById(R.id.departureInput) as EditText
        destinationInput = view?.findViewById(R.id.destinationInput) as EditText

        val departure = departureInput.text.toString()
        val destination = destinationInput.text.toString()

        val fragment: Fragment = CardFragment.newInstance(departure, destination)
        val transaction = activity?.supportFragmentManager!!.beginTransaction()
        //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }



}