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
import kotlinx.android.synthetic.main.activity_select_lib_loc.*
import org.json.JSONArray
import kotlin.collections.ArrayList

class SelectLibLoc : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener {
    class Library_Loc(name:String,address:String,xcnts:Double,ydnts:Double ){
        var name:String=""
        var address:String=""
        var xcnts:Double=0.0
        var ydnts:Double=0.0
        init {
            this.name=name
            this.address=address
            this.xcnts=xcnts
            this.ydnts=ydnts
        }
    }
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val colRef: CollectionReference =db.collection("customers")
    private lateinit var  databaseReference: DatabaseReference
    private lateinit var mMap: GoogleMap
    var Loc_list=ArrayList<Library_Loc>()
    private lateinit var  fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var init_count:Int=0
    var my_latitude:Double=0.0
    var my_longtitude:Double=0.0

    lateinit var maker:Marker
    var count:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_lib_loc)
        databaseReference= FirebaseDatabase.getInstance().reference
        text_library.setText("마커를 클랙해주세요!!")
        var mapgetintent= intent
        val saveid=mapgetintent.getStringExtra("id").toString()

        Log.v("아이디 메시지",saveid)
        checkPermission()
        button.setOnClickListener {
            if(!text_library.text.toString().equals("마커를 클랙해주세요!!")){
                db.collection("customers").whereEqualTo("id", saveid).get()
                    .addOnSuccessListener { doc->
                        Log.v("변화된 메시지","찾기 성공:${doc.size()}")
                        for(index in doc) {
                            var savemapdoc=index.id.toString()
                            Log.v("변화된 메시지","${index.id.toString()}")
                            db.collection("customers").document(savemapdoc)
                                .update("selectLib", text_library.text.toString() )

                        }
                        var maptolistintent= Intent(this, RentalBookList::class.java)
                       startActivity(maptolistintent)
                    }
            }
        }
    }
    fun startProcess(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)
       updateLoaction()

        val jsonString = assets.open("jsons/LibraryLoc.json").reader().readText()
        val jsonArray = JSONArray(jsonString)
        for (index in 0 until jsonArray.length()) {
            val Object = jsonArray.getJSONObject(index)
            val save_Loc=Library_Loc(Object.getString("name"),Object.getString("address"),
                Object.getDouble("xcnts"),Object.getDouble("ydnts"))
            Loc_list.add(save_Loc)
        }
        for(index in 0 until Loc_list.size){
            var temp_Loc:Library_Loc=Loc_list.get(index)
            var locpoint=LatLng(temp_Loc.xcnts,temp_Loc.ydnts)
            mMap.addMarker(MarkerOptions().position(locpoint).title(temp_Loc.name))
            mMap.setOnMarkerClickListener(this)
        }
    }
    override fun onMarkerClick(p0: Marker?): Boolean {
        if (p0 != null) {
            text_library.setText(p0.title.toString())
        }
        return true
    }
    @SuppressLint("MissingPermission")
    fun updateLoaction(){
        val locationRequest=com.google.android.gms.location.LocationRequest.create()
        locationRequest.run {
            priority= com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval=1000
        }
        locationCallback=object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for((i,location) in it.locations.withIndex()){
                        setLastLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }
    fun setLastLocation(lastLocation:Location){
        if(count!=0) maker.remove()
        val LatLNG=LatLng(lastLocation.latitude,lastLocation.longitude)
        val markerOptions=MarkerOptions().position(LatLNG).title("내 위치")
        val cameraPosition=CameraPosition.builder().target(LatLNG).zoom(15.0f).build()
        maker=mMap.addMarker(markerOptions)
        if(init_count==0) {
            //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            val initLatLNG = LatLng(34.791823, 126.365268)
            val initcameraPosition = CameraPosition.builder().target(initLatLNG).zoom(15.0f).build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(initcameraPosition))
        }
        init_count++;
        count++
    }


    val permissions =arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION)
    val PERM_LOCATION=99
    fun checkPermission(){
        var permitted_all=true
        for(permission in permissions){
            val result=ContextCompat.checkSelfPermission(this,permission)
            if(result!=PackageManager.PERMISSION_GRANTED){
                permitted_all=false
                requestPermissions()
                break
            }
        }
        if(permitted_all){
            startProcess()
        }
    }
    fun requestPermissions(){
        ActivityCompat.requestPermissions(this,permissions,PERM_LOCATION)
    }

    fun confrimAgain(){
        AlertDialog.Builder(this)
            .setTitle("권한 승인")
            .setMessage("해당 기능을 사용할려면 권한 승인이 필요합니다.")
            .setPositiveButton("재전송",{_,_->
                requestPermissions()
            }).setNegativeButton("아니요",{_,_->
                finish()
            })
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            99->{
                var granted_all=true
                for(result in grantResults){
                    if(result!=PackageManager.PERMISSION_GRANTED){
                        granted_all=false
                        break
                    }
                }
                if(granted_all){
                    startProcess()
                }
                else{
                    confrimAgain()
                }
            }
        }
    }

}
