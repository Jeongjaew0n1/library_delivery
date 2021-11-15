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
import android.location.*
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_detail_orderpage.*
import kotlinx.coroutines.handleCoroutineException
import org.json.JSONArray
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

class OrderdetailPage : AppCompatActivity(), OnMapReadyCallback{
    private lateinit var mMap: GoogleMap
    private lateinit var mMap2: GoogleMap
    var Loc_list=ArrayList<SelectLibLoc.Library_Loc>()
    private lateinit var  fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var db2: FirebaseFirestore = FirebaseFirestore.getInstance()
    var count:Int=0
    var init_count:Int=0
    lateinit var maker:Marker
    lateinit var locpoint:LatLng
    lateinit var customerpoint:LatLng
    lateinit var orders:Orders
    lateinit var customer:Customer
    var set_mapActivity:Int=0
    var customer_id:String=""
    var threadstart:Boolean=true
    var handlerstart:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_orderpage)
//        checkPermission()
        startProcess()
        if(intent.hasExtra("customer_id")){
            customer_id=intent.getStringExtra("customer_id").toString()
        }
        btn_detailOrder_delivery.setOnClickListener {
            db.collection("Orders").whereEqualTo("customerid", customer_id)
                .get().addOnSuccessListener { doc ->
                    for(index in doc) {
                        db.collection("Orders").document(index.id).update("orderCheck",true)
                    }
                    val args = Bundle()
                    args.putParcelable("libraryPoint",locpoint)
                    args.putParcelable("customerPoint",customerpoint)
                    var deliveryMap_intent= Intent(this, Deliveryman_ShowMap::class.java)
                    deliveryMap_intent.putExtra("locbundle",args)
                    deliveryMap_intent.putExtra("customerid",customer_id)
                    Log.d("배달 버튼","작동")
                    startActivity(deliveryMap_intent)

                }

        }
        val handler=object : Handler(){
            override fun handleMessage(msg: Message){
                Log.d("메시지", "주문 상세 핸들러 실행")
                if(!threadstart){
                    removeMessages(0)
                }
                db.collection("Orders").whereEqualTo("customerid", customer_id)
                    .get().addOnSuccessListener { doc ->
                        Log.d("메시지", "주문 상세 파이어베이스 실행")
                        for (index in doc) {
                            Log.d("메시지", "주문 상세 파이어베이스 조건문 실행")
                            if (index.data["orderCheck"].toString().toBoolean()) {
                                Log.d("메시지", "ordercheck가 true입니다.")
                                Log.d("메시지", "ordercheck=${index.data["orderCheck"].toString()}")
                                handlerstart=true
                            }
                            Log.d("메시지", "${index.data["orderCheck"]}")
                        }
                        if(handlerstart){
                            threadstart=false
                            Log.d("메시지", "threadstart가 거짓")
//                            force_priorMove()
                        }

                    }
            }
        }
        thread(start=true){
            while(threadstart) {
                Log.d("메시지", "주문 상세 스레드 실행")
                handlerstart=false
                Thread.sleep(1000)
                handler?.sendEmptyMessage(0)
            }
        }
    }

    override fun onPause() {
        Log.d("메시지", "주문 목록 Pause")
        threadstart=false
        handlerstart=false
        super.onPause()
    }

    override fun onRestart() {
        Log.d("메시지", "주문 목록 Restart")
        threadstart=true
        handlerstart=false
        super.onRestart()
    }

    override fun onStop() {
        Log.d("메시지", "주문 목록 Stop")
        threadstart=false
        handlerstart=false
        super.onStop()
    }
    fun force_priorMove(){
        Log.d("메시지", "전 화면으로 이동")
        var back_intent= Intent(this, DeliveryOrderList::class.java)
        startActivity(back_intent)
    }
    fun startProcess(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("메시지","onMapReady")
        mMap = googleMap
        mMap2 = googleMap
        db.collection("Orders").whereEqualTo("customerid", customer_id)
            .get().addOnSuccessListener { doc ->
                Log.d("메시지", "파이어스토어 사용 중")
                for (index in doc) {
                    val jsonString = assets.open("jsons/LibraryLoc.json").reader().readText()
                    val jsonArray = JSONArray(jsonString)
                    for (libindex in 0 until jsonArray.length()) {
                        val Object = jsonArray.getJSONObject(libindex)
                        if (Object.getString("name")
                                .equals(index.data["orderlib"].toString())) {
                            locpoint = LatLng(Object.getDouble("xcnts"), Object.getDouble("ydnts"))
                            orders = index.toObject(Orders::class.java)
                            Log.d("메시지","찾은 도서관 이름 "+orders.orderlib.toString())
                            Log.d("메시지","찾은 도서관 위치 "+locpoint.latitude+" "+locpoint.longitude)
                            break
                        }
                    }
                    Log.d("메시지","Orders 데베 접근 완료")
                }
                Log.d("메시지", "데베 데이터 접근 완료")
                mMap2.addMarker(MarkerOptions().position(locpoint).title(orders.orderlib).icon(BitmapDescriptorFactory.fromResource(R.drawable.libraryimg)))
            }
        db2.collection("customers").whereEqualTo("id", customer_id)
            .get().addOnSuccessListener { customdoc ->
                Log.d("메시지","고객 데이터베이스 접근")
                for (customindex in customdoc) {
                    Log.d("메시지","고객 주소 접근")
                    customerpoint = LatLng(
                        customindex.data["xcnts"].toString().toDouble(),
                        customindex.data["ydnts"].toString().toDouble()
                    )
                    Log.d("메시지","고객 위치"+customerpoint.latitude+" "+customerpoint.longitude)
                    detailOrdername.setText(customindex.data["name"].toString())
                    detailOrderid.setText(customindex.data["id"].toString())
                    detailOrderAddress.setText(customindex.data["address"].toString())
                    detailOrderlibrary.setText(customindex.data["selectLib"].toString())
                }
                mMap2.addMarker(MarkerOptions().position(customerpoint).title(customer_id).icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.userimg)))

            }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        updateLoaction()

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
        maker.rotation
        maker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.deliveryimg))
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