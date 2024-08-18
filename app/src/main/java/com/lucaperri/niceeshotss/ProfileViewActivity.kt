package com.lucaperri.niceeshotss

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.lucaperri.niceeshotss.utils.UserProfileObject
import com.lucaperri.niceeshotss.utils.tasks.Task
import com.lucaperri.niceeshotss.utils.tasks.TaskAdapter

class ProfileViewActivity : Activity() {

    lateinit var username : TextView
    lateinit var dob: TextView
    lateinit var posts : TextView
    lateinit var level : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profileview)
        username = findViewById(R.id.profile_uname)
        dob = findViewById(R.id.profile_dob)
        posts = findViewById(R.id.profile_posts)
        level = findViewById(R.id.profile_exp)


        var reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Registered Users")
        var currentUser = FirebaseAuth.getInstance().currentUser!!
        reference.child(currentUser.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var userData : UserProfileObject? = snapshot.getValue(UserProfileObject::class.java)
                if (userData != null) {
                    username.setText(userData.username)
                    dob.setText(userData.dob)
                    posts.setText("Currently no Posts.")
                    level.setText(userData.experience_level.toString()+" Exp")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}