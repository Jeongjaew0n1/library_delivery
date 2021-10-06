package com.example.reigster_show

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_login.*

class login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val intent= Intent(this, MainActivity::class.java)
        btn_signIn.setOnClickListener {
            startActivity(intent)
        }
    }
}