package de.thm.mow.felixwegener.simplydrive.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import de.thm.mow.felixwegener.simplydrive.*

class ProfileFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user_id__View: TextView
    private lateinit var user_email__View: TextView
    private lateinit var logOut__btn: Button
    private lateinit var tv__MainActivity: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        user_id__View = view.findViewById(R.id.user_id__View)
        user_email__View = view.findViewById(R.id.user_email__View)
        logOut__btn = view.findViewById(R.id.logOut__btn)
        tv__MainActivity = view.findViewById(R.id.tv__MainActivity)

        tv__MainActivity.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        logOut__btn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        return view
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser == null) {
            startActivity(Intent(activity, RegisterActivity::class.java))
        } else {
            val email = firebaseUser.email
            val uid = firebaseUser.uid

            user_id__View.text = "User ID :: $uid"
            user_email__View.text = "Email ID :: $email"
        }

    }


}