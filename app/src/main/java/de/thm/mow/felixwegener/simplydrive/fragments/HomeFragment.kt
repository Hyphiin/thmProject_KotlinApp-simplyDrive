package de.thm.mow.felixwegener.simplydrive.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.thm.mow.felixwegener.simplydrive.R
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.cvHomeOne.setOnClickListener { view ->
            onHomeCardClicked()
        }
        view.cvHomeTwo.setOnClickListener { view ->
            onHomeCardClicked()
        }

        view.cvHomeThree.setOnClickListener { view ->
            onHomeCardClicked()
        }

        return view
    }

    private fun onHomeCardClicked(){
        val fragment: Fragment = CardInfoFragment.newInstance("Hanau HBF - ", "Wetzlar Bahnhof")
        val transaction = activity?.supportFragmentManager!!.beginTransaction()
        //transaction.hide(activity?.supportFragmentManager!!.findFragmentByTag("home_fragment")!!)
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}