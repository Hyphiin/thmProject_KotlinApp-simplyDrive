package de.thm.mow.felixwegener.simplydrive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import de.thm.mow.felixwegener.simplydrive.databinding.ActivityProfileBinding
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.nav_header.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        logOut__btn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        tv__MainActivity.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            finish()
        }
        else {
            val email = firebaseUser.email
            val uid = firebaseUser.uid

            user_id__View.text = "User ID :: $uid"
            user_email__View.text = "Email ID :: $email"
        }
    }
}