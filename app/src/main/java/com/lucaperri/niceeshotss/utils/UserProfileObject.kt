package com.lucaperri.niceeshotss.utils

class UserProfileObject {

    var email : String = ""
    var fullname : String = ""
    var dob : String = ""
    var experience : String = ""
    var username: String = ""
    var experience_level : Int = 0
    var currentTasks : String = "car;tree;pet"
    var userid: String ="no_id"

    constructor(email: String, fullname: String, dob: String, experience: String, username: String, userid: String){
        this.email = email
        this.fullname = fullname
        this.dob = dob
        this.experience = experience
        this.username = username
        this.userid = userid
    }
    constructor(){

    }
}