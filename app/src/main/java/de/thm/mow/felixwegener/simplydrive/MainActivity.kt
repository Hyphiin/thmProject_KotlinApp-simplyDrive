    package de.thm.mow.felixwegener.simplydrive

    import android.annotation.SuppressLint
    import android.content.Intent
    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import android.view.View
    import android.view.animation.Animation
    import android.view.animation.AnimationUtils
    import androidx.fragment.app.Fragment
    import de.thm.mow.felixwegener.simplydrive.databinding.ActivityMainBinding
    import de.thm.mow.felixwegener.simplydrive.fragments.*
    import kotlinx.android.synthetic.main.activity_main.*






    class MainActivity : AppCompatActivity() {

        private lateinit var binding: ActivityMainBinding

        //Fragments
        private val homeFragment = HomeFragment()
        private val settingsFragment = SettingsFragment()
        private val historyFragment = HistoryFragment()
        private val editFragment = EditFragment()
        private val scanFragment = ScanFragment()


        //FAB Button(s)
        private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
        private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
        private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
        private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

        private var clicked = false

        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            bottomNavigationView.background = null
            bottomNavigationView.menu.getItem(4).isEnabled = false


            //Fragments
            replaceFragment(homeFragment)

            bottomNavigationView.setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_home -> replaceFragment(homeFragment)
                    R.id.nav_setting -> replaceFragment(settingsFragment)
                    R.id.nav_history -> replaceFragment(historyFragment)
                    R.id.nav_map -> replaceMapActivity()
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
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }

        }

        private fun replaceMapActivity() {
            startActivity(Intent(this@MainActivity, GpsActivity::class.java))
            finish()

        }


        private fun replaceFragment(fragment: Fragment) {
            fab_main.setImageDrawable(resources.getDrawable(R.drawable.ic_add, this.theme));
            if (clicked){
                onAddButtonClicked()
            }
            if (fragment != null) {
                val fragmentTransaction =  supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, fragment)
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
            if(!clicked){
                fab_scan.visibility = View.VISIBLE
                fab_edit.visibility = View.VISIBLE
            } else {
                fab_scan.visibility = View.INVISIBLE
                fab_edit.visibility = View.INVISIBLE
            }
        }

         private fun setAnimation(clicked: Boolean) {
            if(!clicked){
                fab_edit.startAnimation(fromBottom)
                fab_scan.startAnimation(fromBottom)
            } else {
                fab_edit.startAnimation(toBottom)
                fab_scan.startAnimation(toBottom)
            }
         }

        private fun setClickable (clicked: Boolean){
            if(!clicked){
                fab_scan.isClickable= true
                fab_edit.isClickable= true
            }else{
                fab_scan.isClickable= false
                fab_edit.isClickable= false
            }
        }

    }