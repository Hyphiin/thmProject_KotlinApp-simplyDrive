package de.thm.mow.felixwegener.simplydrive

public class Route {
    public var date = ""
    public var time = ""
    public var route = ""
    public var uid = ""


    public fun Route(){

    }

    public fun Route(name: String, time: String, route: String, uid: String){
        this.date = date
        this.time = time
        this.route = route
        this.uid = uid

    }

    @JvmName("getDate1")
    public fun getDate(): String {
        return date
    }

    @JvmName("setDate1")
    public fun setDate(date: String){
        this.date = date
    }

    @JvmName("getTime1")
    public fun getTime(): String {
        return time
    }

    @JvmName("getRoute1")
    public fun getRoute(): String {
        return route
    }

    @JvmName("getUid1")
    public fun getUid(): String {
        return uid
    }

}
