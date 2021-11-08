package de.thm.mow.felixwegener.simplydrive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.nav_header.*

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userId = intent.getStringExtra("user_id")
        val emailId = intent.getStringExtra("email_id")

        Log.d("----------------", "$userId")

        user_id__View.text = "User ID :: $userId"
        user_email__View.text = "Email ID :: $emailId"

        logOut__btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
            finish()
        }

        tv__MainActivity.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
            finish()
        }
    }
}