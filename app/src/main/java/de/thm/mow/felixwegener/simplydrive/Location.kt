package de.thm.mow.felixwegener.simplydrive

data class Location(var location: LocationResultSelf? = null, var uid: String? = null, var routeId: String? = null)


data class LocationResultSelf(var lastLocation: LastLocation? = null, var locations: LocationPoint? = null)

data class LastLocation(var accuracy: Float? = null, var altitude: Double? = null, var latitude: Double? = null, var longitude: Double? = null, var provider: String? = null, var speed: Float? = null, var speedAccuracyMetersPerSecond: Float? = null, var time: Long? = null, var verticalAccuracyMeters: Float? = null)

data class LocationPoint(var accuracy: Float? = null, var altitude: Double? = null, var latitude: Double? = null, var longitude: Double? = null, var provider: String? = null, var speed: Float? = null, var speedAccuracyMetersPerSecond: Float? = null, var time: Long? = null, var verticalAccuracyMeters: Float? = null)