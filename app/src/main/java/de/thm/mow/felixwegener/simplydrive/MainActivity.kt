package de.thm.mow.felixwegener.simplydrive

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.thm.mow.felixwegener.simplydrive.databinding.ActivityMainBinding
import de.thm.mow.felixwegener.simplydrive.fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.android.gms.tasks.OnFailureListener

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.storage.StorageReference




class MainActivity : AppCompatActivity(), ScanFragment.OnDataPass, EditFragment.OnDataPass {

    private lateinit var binding: ActivityMainBinding

    //Fragments
    private val homeFragment = HomeFragment()
    private val settingsFragment = SettingsFragment()
    private val historyFragment = HistoryFragment()
    private val editFragment = EditFragment()
    private val scanFragment = ScanFragment()
    private val mapsFragment = MapsFragment()
    private val profileFragment = ProfileFragment()

    private lateinit var userPic: ImageView
    var storage = Firebase.storage
    private lateinit var firebaseAuth: FirebaseAuth



    //FAB Button(s)
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    private var clicked = false

    private lateinit var currentDriveId: String

    override fun onDataPass(data: String) {
        Log.d("LOG", "hello $data")
        currentDriveId = data

        // set
        (this.application as MyApplication).setDriveId(currentDriveId)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(4).isEnabled = false

        firebaseAuth = FirebaseAuth.getInstance()

        retrieveProfilePic()

        //Fragments
        replaceFragment(homeFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> replaceFragment(homeFragment)
                R.id.nav_setting -> replaceGpsActivity()
                R.id.nav_history -> replaceFragment(historyFragment)
                R.id.nav_map -> replaceFragment(mapsFragment)
            }
            true
        }

        //FAB Button(s)
        fab_main.setOnClickListener {
            onAddButtonClicked()
            fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_add, this.theme));
        }
        fab_scan.setOnClickListener {
            replaceFragment(scanFragment)
            fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_scanner, this.theme));

        }
        fab_edit.setOnClickListener {
            replaceFragment(editFragment)
            fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_search, this.theme));
        }

        //PROFILE
        profilePic.setOnClickListener {
            replaceFragment(profileFragment)
        }

    }

    private fun retrieveProfilePic() {
        val firebaseUser = firebaseAuth.currentUser

        if (firebaseUser != null) {
            val imageRef = storage.reference.child("images/${firebaseUser.uid}")

            val ONE_MEGABYTE = (1024 * 1024)*2000.toLong()
            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                // Data for "images/island.jpg" is returns, use this as needed
                Log.d("userPic:", imageRef.path.toString())
                val bitmap = BitmapFactory.decodeFile(imageRef.path)
                userPic.setImageBitmap(bitmap)
            }.addOnFailureListener {
                // Handle any errors
            }
        }
    }

    private fun replaceGpsActivity() {
        startActivity(Intent(this@MainActivity, GpsActivity::class.java))
        finish()
    }

    private fun replaceMapActivity() {
        startActivity(Intent(this@MainActivity, MapsActivity::class.java))
        finish()
    }


    private fun replaceFragment(fragment: Fragment) {
        fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_add, this.theme));
        if (clicked) {
            onAddButtonClicked()
        }
        if (fragment != null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainer, fragment, "fragmentTag")
            fragmentTransaction.commit()
        }
    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            fab_scan.visibility = View.VISIBLE
            fab_edit.visibility = View.VISIBLE
        } else {
            fab_scan.visibility = View.INVISIBLE
            fab_edit.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            fab_edit.startAnimation(fromBottom)
            fab_scan.startAnimation(fromBottom)
        } else {
            fab_edit.startAnimation(toBottom)
            fab_scan.startAnimation(toBottom)
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            fab_scan.isClickable = true
            fab_edit.isClickable = true
        } else {
            fab_scan.isClickable = false
            fab_edit.isClickable = false
        }
    }

    private lateinit var currentDrive: String

    private fun setDrive(d: String) {
        currentDrive = d
    }

}