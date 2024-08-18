package com.lucaperri.niceeshotss

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    lateinit var loginButton: Button
    lateinit var registerBtn: TextView
    public val context: Context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title = "Login"

        loginButton = findViewById(R.id.ui_login_lgbtn)
        registerBtn = findViewById(R.id.ui_login_register)

        loginButton.setOnClickListener {
            var intent : Intent = Intent(context, LoginFormActivity::class.java)
            startActivity(intent)
        }
        registerBtn.setOnClickListener {
            var intent : Intent = Intent(context, RegisterFormActivity::class.java)
            startActivity(intent)
        }
    }


}