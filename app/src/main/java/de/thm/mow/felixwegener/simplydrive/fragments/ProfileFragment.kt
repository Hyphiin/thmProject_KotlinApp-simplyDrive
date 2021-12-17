package de.thm.mow.felixwegener.simplydrive.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.thm.mow.felixwegener.simplydrive.R
import de.thm.mow.felixwegener.simplydrive.RegisterActivity
import android.provider.MediaStore
import java.io.IOException
import com.google.firebase.storage.UploadTask
import android.widget.Toast
import com.google.firebase.storage.StorageReference
import android.app.ProgressDialog
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.storage.OnProgressListener
import java.io.File


class ProfileFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user_id__View: TextView
    private lateinit var user_email__View: TextView
    private lateinit var logOut__btn: Button
    private lateinit var tv__MainActivity: TextView
    private lateinit var btn__ImgSelect: Button
    private lateinit var btn__ImgUpload: Button
    private lateinit var img__ProfilePic: ImageView

    // Uri indicates, where the image will be picked from
    private var filePath: Uri? = null

    // request code
    private val PICK_IMAGE_REQUEST = 22

    var storage = Firebase.storage


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        user_id__View = view.findViewById(R.id.user_id__View)
        user_email__View = view.findViewById(R.id.user_email__View)
        logOut__btn = view.findViewById(R.id.logOut__btn)
        tv__MainActivity = view.findViewById(R.id.tv__MainActivity)
        btn__ImgSelect = view.findViewById(R.id.btn__ImgSelect)
        btn__ImgUpload = view.findViewById(R.id.btn__ImgUpload)
        img__ProfilePic = view.findViewById(R.id.img__ProfilePic)

        tv__MainActivity.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        retrieveProfilePic()

        logOut__btn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        btn__ImgSelect.setOnClickListener {
            selectImage()
        }

        btn__ImgUpload.setOnClickListener {
            uploadImage()
        }

        return view
    }

    private fun uploadImage() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            if (filePath != null) {

                // Code for showing progressDialog while uploading
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                // Defining the child of storageReference
                val ref: StorageReference = storage.reference
                    .child(
                        "images/"
                                + firebaseUser.uid
                    )

                // adding listeners on upload
                // or failure of image
                ref.putFile(filePath!!)
                    .addOnSuccessListener { // Image uploaded successfully
                        // Dismiss dialog
                        progressDialog.dismiss()
                        Toast
                            .makeText(
                                activity,
                                "Image Uploaded!!",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                    .addOnFailureListener { e -> // Error, Image not uploaded
                        progressDialog.dismiss()
                        Toast
                            .makeText(
                                activity,
                                "Failed " + e.message,
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
                    .addOnProgressListener(
                        object : OnProgressListener<UploadTask.TaskSnapshot?> {
                            // Progress Listener for loading
                            // percentage on the dialog box
                            override fun onProgress(
                                taskSnapshot: UploadTask.TaskSnapshot
                            ) {
                                val progress = ((100.0
                                        * taskSnapshot.bytesTransferred
                                        / taskSnapshot.totalByteCount))
                                progressDialog.setMessage(
                                    ("Uploaded "
                                            + progress.toInt() + "%")
                                )
                            }
                        })
            }
        }
    }

    private fun selectImage() {
        // Defining Implicit Intent to mobile gallery
        // Defining Implicit Intent to mobile gallery
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image from here..."
            ),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {

            // Get the Uri of data
            filePath = data.data
            try {

                // Setting image on image view using Bitmap
                val bitmap = MediaStore.Images.Media
                    .getBitmap(
                        activity?.contentResolver,
                        filePath
                    )
                img__ProfilePic.setImageBitmap(bitmap)
            } catch (e: IOException) {
                // Log the exception
                e.printStackTrace()
            }
        }
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

    private fun retrieveProfilePic() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null) {
            val imageRef = storage.reference.child("images/${firebaseUser.uid}")


            val localFile = File.createTempFile("tempImage", "jpg")
            imageRef.getFile(localFile).addOnSuccessListener {
                Log.d("userPic:", imageRef.path.toString())
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                img__ProfilePic.setImageBitmap(bitmap)
            }.addOnFailureListener {
                // Handle any errors
            }
        }
    }


}