package com.lucaperri.niceeshotss

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.values
import com.lucaperri.niceeshotss.utils.UserProfileObject

class LoginFormActivity : Activity(), View.OnClickListener {

    lateinit var loginbtn : Button
    lateinit var mail : EditText
    lateinit var pwd : EditText
    lateinit var progressBar: ProgressBar
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginform)

        loginbtn = findViewById(R.id.ui_login_loginbutton)
        loginbtn.setOnClickListener(this)

        mail = findViewById(R.id.ui_login_usernameinput)
        pwd = findViewById(R.id.ui_login_fullnameinput)
        progressBar = findViewById(R.id.ui_login_progressbar)
        auth = FirebaseAuth.getInstance()

    }

    override fun onClick(v: View?) {
        var email = mail.text.toString()
        var pwd = this.pwd.text.toString()

        if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "You have to enter a email to log in.", Toast.LENGTH_LONG).show()

        }else if(TextUtils.isEmpty(pwd)){
            Toast.makeText(this, "Please enter your password to log in.", Toast.LENGTH_LONG).show()
        }else{
            progressBar.visibility = ProgressBar.VISIBLE
            loginUser(email, pwd)
        }
    }
    private fun loginUser(email : String, pwd : String){
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener {
            task -> run {
                if(task.isSuccessful){

                    var intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else{
                    try {
                        throw task.exception!!
                    }catch (e: FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(this, "Invalid credentials!", Toast.LENGTH_LONG).show()
                    }
                }
            progressBar.visibility = ProgressBar.GONE
            }
        }
    }
    override fun onStart() {
        super.onStart()
        var reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Registered Users")
        var user = auth?.currentUser
        if(auth.currentUser != null){
            if (user != null) {
                reference.child(user.uid).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var userData : UserProfileObject? = snapshot.getValue(UserProfileObject::class.java)
                        if(userData == null) auth.signOut()
                        return
                    }

                    override fun onCancelled(error: DatabaseError) {
                        auth.signOut()
                        return
                    }
                })
                var intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }
}