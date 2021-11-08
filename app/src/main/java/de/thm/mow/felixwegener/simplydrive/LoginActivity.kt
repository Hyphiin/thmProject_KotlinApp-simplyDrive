package de.thm.mow.felixwegener.simplydrive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.registerLoginField

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        registerLoginField.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

        loginLoginBtn.setOnClickListener{
            when {
                TextUtils.isEmpty(loginEmailInput.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(this@LoginActivity, "Bitte Email eingeben.", Toast.LENGTH_SHORT)
                        .show()
                }

                TextUtils.isEmpty(
                    loginPasswordInput.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Bitte Passwort eingeben.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val email: String = loginEmailInput.text.toString().trim { it <= ' '}
                    val password: String = loginPasswordInput.text.toString().trim { it <= ' '}

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@LoginActivity, "Erfolgreich eingeloggt!", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra("user_id", FirebaseAuth.getInstance().currentUser!!.uid)
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
}