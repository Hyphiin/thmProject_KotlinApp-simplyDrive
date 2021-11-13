    package de.thm.mow.felixwegener.simplydrive

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
        //private val icAdd =

        private var clicked = false

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
                }
                true
            }

            //FAB Button(s)
            fab_main.setOnClickListener {
                onAddButtonClicked()
            }
            fab_scan.setOnClickListener {
                replaceFragment(scanFragment)
            }
            fab_edit.setOnClickListener {
                replaceFragment(editFragment)
            }


        }


        private fun replaceFragment(fragment: Fragment) {
            if (fragment != null) {
                val fragmentTransaction =  supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, fragment, "history_fragment")
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