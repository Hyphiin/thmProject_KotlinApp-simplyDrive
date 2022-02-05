package de.thm.mow.felixwegener.simplydrive

import android.app.Application
import android.location.Location
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
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

    private var startDrive: Boolean? = true

    fun getStartDrive(): Boolean? {
        return startDrive
    }

    fun setStartDrive(startDrive: Boolean?) {
        this.startDrive = startDrive
    }

    private var driveId: String? = "null"

    fun getDriveId(): String? {
        return driveId
    }

    fun setDriveId(currentDriveId: String?) {
        this.driveId = currentDriveId
    }

    private var currentLocation: Location? = null
    fun getCurrentLocation(): Location? {
        return currentLocation
    }
    fun setCurrentLocation(location: Location) {
        this.currentLocation = location
    }


    private var allMode: Boolean? = false
    fun getAllMode(): Boolean? {
        return allMode
    }
    fun setAllMode(boolean: Boolean) {
        this.allMode = boolean
    }

    private var twoMode: Boolean? = false
    fun getTwoMode(): Boolean? {
        return twoMode
    }
    fun setTwoMode(boolean: Boolean) {
        this.twoMode = boolean
    }

    private var cellOnlyMode: Boolean? = false
    fun getCellOnlyMode(): Boolean? {
        return cellOnlyMode
    }
    fun setCellOnlyMode(boolean: Boolean) {
        this.cellOnlyMode = boolean
    }

}
