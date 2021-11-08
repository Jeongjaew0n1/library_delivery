package com.example.reigster_show
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.location.*
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_deliveryman_showmap.*
import android.net.Uri
import android.telephony.SmsManager
import android.app.PendingIntent
import android.widget.Toast


class Deliveryman_ShowMap : AppCompatActivity(), OnMapReadyCallback{
    lateinit var locpoint:LatLng
    lateinit var customerpoint:LatLng
    var customer_id:String=""
    var customer_phone:String=""
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val REQUEST_CODE = 100
    val SMS_RECEIVE_PERMISSON = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliveryman_showmap)
        startProcess()
        val locbundle = intent.getParcelableExtra<Bundle>("locbundle")
        if (locbundle != null) {
            locpoint= locbundle.getParcelable("libraryPoint")!!
            customerpoint=locbundle.getParcelable("customerPoint")!!
        }
        if(intent.hasExtra("customerid")){
            customer_id=intent.getStringExtra("customerid").toString()
            Log.d("배달맵 메시지",customer_id)
        }


        val permissonCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        val permissions =arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)
        if(permissonCheck == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "SMS 수신권한 있음", Toast.LENGTH_SHORT).show()
            btn_deliveryStartsend.setOnClickListener {
                access_database(customer_id,1)

            }
            btn_deliveryArrivesend.setOnClickListener {
                access_database(customer_id,2)
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "SMS 수신권한 없음", Toast.LENGTH_SHORT).show()
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)){
                ActivityCompat.requestPermissions(this,  permissions, SMS_RECEIVE_PERMISSON);
            }
            else{
                ActivityCompat.requestPermissions(this, permissions, SMS_RECEIVE_PERMISSON)
            }
        }
    }
    fun startProcess(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.deliverymapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    private fun sendMMSIntent(message: String,phonenum:String) {
        val uri = Uri.parse("smsto:010-3165-2121")
        val sendIntent = Intent(Intent.ACTION_SENDTO, uri)
//        sendIntent.type = "vnd.android-dir/mms-sms"
        sendIntent.putExtra("address", phonenum)
        sendIntent.putExtra("sms_body", message)
        startActivity(sendIntent)
    }
    fun access_database(id:String,check:Int){
        db.collection("customers").whereEqualTo("id",id)
            .get().addOnSuccessListener { doc->
                for (index in doc) {
                    customer_phone=invert_Phonenum(index.data["phone"].toString())
                }
            }
        try {
            val sms = SmsManager.getDefault()
            val phones = "01031652121"

            if (check == 1) {
                sms.sendTextMessage(phones, null, "출발했습니다.", null, null);
            } else if (check == 2) {
                sms.sendTextMessage(phones, null, "도착하기 3분 전 입니다.", null, null);
            }
            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show()
        }
        catch (e: Exception ){
            Toast.makeText(getApplicationContext(), "전송 오류!", Toast.LENGTH_LONG).show()
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show()//오류 원인이 찍힌다.
            e.printStackTrace();
        }
    }
    fun invert_Phonenum(phone:String):String{
        var invertPhonenum :String= ""
        if(phone.length==11) {
            var ran=IntRange(0,2)
            var ran2=IntRange(3,6)
            var ran3=IntRange(7,10)
            invertPhonenum=phone.slice(ran)+"-"+phone.slice(ran2)+"-"+phone.slice(ran3)
        }
        Log.d("배달맵 메시지",invertPhonenum)

        return invertPhonenum

    }

    override fun onMapReady(googleMap: GoogleMap) {

    }

}