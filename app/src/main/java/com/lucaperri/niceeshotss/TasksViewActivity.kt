package com.lucaperri.niceeshotss

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values
import com.lucaperri.niceeshotss.utils.UserProfileObject
import com.lucaperri.niceeshotss.utils.tasks.Task
import com.lucaperri.niceeshotss.utils.tasks.TaskAdapter

class TasksViewActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tasksview)
        var reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Registered Users")
        var currentUser = FirebaseAuth.getInstance().currentUser!!

        reference.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemList = listOf(
                    Task("No Task", "There's no task in ths queue currently. Tasks can be found through posting", 0),
                    Task("No Task", "There's no task in ths queue currently. Tasks can be found through posting", 0),
                    Task("No Task", "There's no task in ths queue currently. Tasks can be found through posting", 0)
                )
                var userData : UserProfileObject? = snapshot.getValue(UserProfileObject::class.java)
                var listOfStrings = userData?.currentTasks?.split(";")
                if (listOfStrings != null) {
                    var i : Int = 0
                    var list: MutableList<Task> = mutableListOf()
                    for(s in listOfStrings){
                        list.add(Task(s.toUpperCase(), "Please make a picture of a "+s+" and post it. Make sure to take a good" +
                                " snap, as people can judge your picture!", 100))
                    }
                    while(list.size < 3){
                        list.add(Task("No Task", "There's no task in ths queue currently. Tasks can be found through posting", 0))
                    }
                    itemList = list
                }
                var recyclerView = findViewById<RecyclerView>(R.id.ui_taskview_recycle)
                recyclerView.layoutManager = LinearLayoutManager(this@TasksViewActivity)
                recyclerView.adapter = TaskAdapter(itemList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }


        })


    }


}