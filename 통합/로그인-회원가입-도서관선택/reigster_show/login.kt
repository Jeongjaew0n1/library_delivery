package com.example.reigster_show

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*


class login : AppCompatActivity() {
    var customs= arrayListOf<Customer>()
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val colRef: CollectionReference =db.collection("customers")
    var save_id:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val register_intent=Intent(this, MainActivity::class.java)
        val map_intent=Intent(this,SelectLibLoc::class.java)
        var logincheck:Boolean=false
        btn_register.setOnClickListener {
            startActivity(register_intent)
            finish()
        }
        btn_login.setOnClickListener {
            val e_check=empty_check()
            var deliverid:String=""
            if(e_check==1) {
                db.collection("customers").get().addOnSuccessListener { result ->
                    for (index in result) {
                        var temp_id: String = index.data["id"] as String
                        var temp_passwd:String = index.data["passwd"] as String
                        if (temp_id.equals(text_id.text.toString())&&temp_passwd.equals(text_passwd.text.toString())) {
                            deliverid=temp_id
                            logincheck = true
                            break
                        }
                    }
                    if (logincheck) {
                        map_intent.putExtra("id", deliverid)
                        startActivity(map_intent)
                        finish()
                    } else {
                        text_siutation.setText("입력한 정보가 틀렸습니다.")
                        text_id.setText("")
                        text_passwd.setText("")
                    }
                }
            }
            else{
                if(e_check==-1) text_siutation.setText("아이디,비밀번호 입력란이 비어있습니다.")
                else if(e_check==-2)  text_siutation.setText("아이디 입력란이 비어있습니다.")
                else text_siutation.setText("비밀번로 입력란이 비어있습니다.")
                text_id.setText("")
                text_passwd.setText("")
            }
        }
    }
    fun empty_check():Int{
        var check:Int=0
        if(text_id.text.toString().equals("") && text_passwd.text.toString().equals("")){
                check=-1
        }
        else if(text_id.text.toString().equals("")){
                check=-2
        }
        else if(text_passwd.text.toString().equals("")){
                check=-3
        }
       check=1
       return check
    }


}

