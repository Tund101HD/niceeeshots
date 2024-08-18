package com.lucaperri.niceeshotss

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import com.lucaperri.niceeshotss.utils.UserProfileObject
import java.lang.Exception
import java.util.Calendar

class RegisterFormActivity : Activity(), AdapterView.OnItemSelectedListener  {

    var experience : String = "Beginner (Hobbyist)"
    lateinit var username : EditText
    lateinit var fullname : EditText
    lateinit var uemail: EditText
    lateinit var dob: EditText
    lateinit var pwd: EditText
    lateinit var sumbitBtn : Button
    lateinit var progressBar: ProgressBar
    lateinit var picker : DatePickerDialog
    private final var tag = "RegisterFormActivity"

    override fun onCreate(savedInstanceState: Bundle?) { //WHAT THE FUCK
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registerform)
        Toast.makeText(this, "View created", Toast.LENGTH_LONG).show()
        username = findViewById(R.id.ui_register_usernameinput)
        fullname = findViewById(R.id.ui_register_fullnameinput)
        uemail = findViewById(R.id.ui_register_emailinput)
        dob = findViewById(R.id.ui_register_birthinput)
        dob.setOnClickListener {
            var cal = Calendar.getInstance()
            var day = cal.get(Calendar.DAY_OF_MONTH)
            var month = cal.get(Calendar.MONTH)
            var year = cal.get(Calendar.YEAR)

            picker = DatePickerDialog(this)

            picker.setOnDateSetListener { view, year, month, dayOfMonth ->
                run {
                    var builder = StringBuilder().append(dayOfMonth).append("/").append(month+1).append("/").append(year)
                    dob.setText(builder.toString())
                }
            }
            picker.show()
        }

        pwd = findViewById(R.id.ui_register_pwdinput)
        sumbitBtn = findViewById(R.id.ui_register_registersumbit)
        progressBar = findViewById(R.id.ui_register_progressbar)

        //Submit button
        sumbitBtn.setOnClickListener {
            var user : String = username.text.toString()
            var name : String = fullname.text.toString()
            var mail : String = uemail.text.toString()
            var birth : String = dob.text.toString()
            var password : String = pwd.text.toString()
            if(TextUtils.isEmpty(user)) {
                Toast.makeText(this, "A username is required!", Toast.LENGTH_LONG)
            }else if(TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Your full name is required!", Toast.LENGTH_LONG)
            }else if(TextUtils.isEmpty(mail)) {
                Toast.makeText(this, "Your email is required!", Toast.LENGTH_LONG)
            }else if(TextUtils.isEmpty(birth)) {
                Toast.makeText(this, "Your Day of Birth is required!", Toast.LENGTH_LONG)
            }else if(TextUtils.isEmpty(password)) {
                Toast.makeText(this, "You need to provide a password!", Toast.LENGTH_LONG)
            }else{
                progressBar.visibility=ProgressBar.VISIBLE
                registerUser(user, name, mail, birth, password, experience)
            }

        }

        val spinner: Spinner = findViewById(R.id.ui_register_jobinput)
        ArrayAdapter.createFromResource(
            this,
            R.array.register_selections,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        experience = parent?.getItemAtPosition(position).toString()
        Toast.makeText(this, "You selected: "+ parent?.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        parent?.setSelection(0)
    }

    fun registerUser(username : String, name : String, email : String, dob : String, pwd : String, experience : String){
        var auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(this, OnCompleteListener<AuthResult>() {
            task ->
            //On complete for the user creation
            run {
                if (task.isSuccessful) {
                    var currentUser = auth.currentUser!!
                    var reference = FirebaseDatabase.getInstance("https://niceeshotss-default-rtdb.europe-west1.firebasedatabase.app").getReference("Registered Users")
                    var db1 = UserProfileObject(this.uemail.text.toString(), this.fullname.text.toString(), this.dob.text.toString(), this.experience, this.username.text.toString(), currentUser.uid)
                    reference.child(currentUser.uid).setValue(db1).addOnCompleteListener {
                        //on complete to the userinformation
                        task -> run {
                            if(task.isSuccessful){
                                currentUser?.sendEmailVerification()

                                var intent = Intent(this, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }else{
                                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show()
                                progressBar.visibility = ProgressBar.GONE
                            }
                        }
                    }

                }else{
                    progressBar.visibility = ProgressBar.GONE
                    try {
                        throw task.exception!!
                    }catch (e: FirebaseAuthWeakPasswordException){
                        Toast.makeText(this, "Sorry, but your password is too weak!", Toast.LENGTH_LONG).show()
                        this.pwd.setError("Password is to weak!")
                        this.pwd.requestFocus()
                    }catch (e: FirebaseAuthUserCollisionException){
                        Toast.makeText(this, "Sorry, but a user already exists with this email!", Toast.LENGTH_LONG).show()
                        uemail.setError("Email is already in use!")
                        uemail.requestFocus()
                    }catch (e: FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(this, "Sorry, but we couldn't identify your email!", Toast.LENGTH_LONG).show()
                        uemail.setError("Coudln't identify email!")
                        uemail.requestFocus()
                    }catch (es: Exception){
                        es.message?.let { Log.e(tag, it) }
                    }

                }
            }
        })
    }
}