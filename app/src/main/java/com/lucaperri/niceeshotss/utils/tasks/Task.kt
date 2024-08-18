package com.lucaperri.niceeshotss.utils.tasks

class Task {

    var title = "No_Title"
    var description = "No Description"
    var exp : Int
    constructor(title: String, description: String, exp: Int){
        this.title = title
        this.description = description
        this.exp = exp
    }
}