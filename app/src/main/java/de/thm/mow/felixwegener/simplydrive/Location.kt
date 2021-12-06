package de.thm.mow.felixwegener.simplydrive

import com.google.android.gms.location.LocationResult

data class Location(var location: LocationResult, var uid: String, var routeId: String)
