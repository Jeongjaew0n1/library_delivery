package com.example.reigster_show

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent2= Intent(this, login::class.java)
        btn_confirm.setOnClickListener {
            var check:Boolean
            check=empty_check(input_name.text.toString(),input_Id.text.toString(),input_Password.text.toString()
                        ,input_Phone.text.toString(),input_Address.text.toString())
            if(check){
                //끝
            }
            else{
                //
            }
        }

        btn_cancel.setOnClickListener {
            startActivity(intent2)
        }

        }
    fun empty_check(name:String,id:String,passwd:String,phone:String,address:String,):Boolean{
        if(name.length==0 || id.length==0 || passwd.length==0 || phone.length==0 || address.length==0){
            return false;
        }
        return true;
    }
    fun id_overlap_check(id:String):Boolean{
        if(true){

        }
        return true;
    }


    }
