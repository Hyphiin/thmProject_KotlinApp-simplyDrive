package de.thm.mow.felixwegener.simplydrive

import android.app.Application
import android.location.Location

class MyApplication : Application() {
    private var myLocations: List<Location>? = null
    fun getMyLocations(): List<Location>? {
        return myLocations
    }

    fun setMyLocation(myLocations: List<Location>?) {
        this.myLocations = myLocations
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        myLocations = ArrayList()
    }

    companion object {
        private val field: MyApplication? = null
        var instance: MyApplication? = null
            get() = Companion.field
            private set
    }
}
