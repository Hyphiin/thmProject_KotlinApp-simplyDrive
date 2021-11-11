package de.thm.mow.felixwegener.simplydrive

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerRegisterBtn.setOnClickListener {
            when {
                TextUtils.isEmpty(registerEmailInput.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(this@RegisterActivity, "Bitte Email eingeben.", Toast.LENGTH_SHORT).show()
                }

                TextUtils.isEmpty(registerPasswordInput.text.toString().trim { it <= ' '}) -> {
                    Toast.makeText(this@RegisterActivity, "Bitte Passwort eingeben.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val email: String = registerEmailInput.text.toString().trim { it <= ' '}
                    val password: String = registerPasswordInput.text.toString().trim { it <= ' '}

                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->
                                if (task.isSuccessful) {
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(this@RegisterActivity, "Erfolgreich registriert!", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@RegisterActivity, ProfileActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra("user_id", firebaseUser.uid)
                                    intent.putExtra("email_id", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this@RegisterActivity, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                }
            }
        }

        register__google.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            //Build a GoogleSignInClient with the options specified by gso.
            val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        }

        registerLoginField.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }
}