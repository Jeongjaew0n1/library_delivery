package com.example.reigster_show

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.CountDownLatch

import android.widget.TextView





class MainActivity : AppCompatActivity() {
    var db:FirebaseFirestore= FirebaseFirestore.getInstance()
    val colRef: CollectionReference=db.collection("customers")
    var customs= arrayListOf<Customer>()
    var check:Boolean=false

    private lateinit var  databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("메시지", "회원가입 페이지")
        databaseReference=FirebaseDatabase.getInstance().reference
        btn_confirm.setOnClickListener {
            var empty_confirm=empty_check()
            if(empty_confirm) text_situation.setText("비어있는 정보란 존재")
            else{
                check=false

                Log.d("메시지", "데베접근 ")
                db.collection("customers").get().addOnSuccessListener { result ->
                    for (index in result) {
                        Log.d("메시지","${index.data["id"]}")
                        Log.d("메시지", "객체get중")
                        var temp_id:String=index.data["id"] as String
                        Log.d("메시지 temp", temp_id as String)
                            if (temp_id.equals(input_Id.text.toString())) {
                                                Log.d("메시지", "검사 중")
                                                input_Id.setText("")
                                                text_situation.setText("아이디가 중복됩니다...")
                                                check = true
                                                break
                                            }
                        }
                   if(!check){
                       addCustomer()
                       var login_intent= Intent(this, login::class.java)
                       startActivity(login_intent)
                       finish()
                   }

                }
            }
        }
        btn_cancel.setOnClickListener {
            var login_intent= Intent(this, login::class.java)
            startActivity(login_intent)
            finish()
        }

    }
    fun empty_check():Boolean{
        if(input_Id.text.toString().equals("")||input_Address.text.toString().equals("")||input_Password.text.toString().equals("")||
                input_Phone.text.toString().equals("")||input_name.text.toString().equals("")){
            return true;
        }
        return false;
    }
    fun addCustomer(){
        val custom= hashMapOf(
            "id" to input_Id.text.toString(),
            "passwd" to input_Password.text.toString(),
            "name" to input_name.text.toString(),
            "address" to input_Address.text.toString(),
            "phone" to input_Phone.text.toString(),
            "selectLib" to "없음"
        )
        db.collection("customers").add(custom).addOnSuccessListener {
            Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
        }
    }
}