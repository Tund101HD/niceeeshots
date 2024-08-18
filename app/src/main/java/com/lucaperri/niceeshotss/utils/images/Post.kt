package com.lucaperri.niceeshotss.utils.images

import android.widget.TextView

class Post {

    lateinit var imgname: String
    lateinit var date: String
    lateinit var iso: String
    lateinit var focal: String
    lateinit var exposure: String
    lateinit var latitude: String
    lateinit var longitude: String
    lateinit var fov: String
    lateinit var model: String
    lateinit var size: String
    constructor(uri: String, imgname: String, date: String, iso: String, focal:String, exposure: String, lat: String, long: String, fov : String, model: String, size: String){
        this.imgname = imgname
        this.date = date
        this.iso = iso
        this.focal = focal
        this.exposure = exposure
        this.latitude = lat
        this.longitude = long
        this.fov = fov
        this.model = model
        this.size = size
    }
    constructor(){}
}