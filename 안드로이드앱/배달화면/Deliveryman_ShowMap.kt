package com.example.reigster_show
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import android.location.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_deliveryman_showmap.*
import android.net.Uri
import android.telephony.SmsManager
import android.content.ContentValues
import android.graphics.Color
import android.os.*
import android.widget.Toast
import androidx.core.graphics.rotationMatrix
import com.google.android.gms.maps.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread
class Deliveryman_ShowMap : AppCompatActivity(), OnMapReadyCallback {
    lateinit var locpoint: LatLng
    lateinit var customerpoint: LatLng
    lateinit var deliverypoint: LatLng

    var customer_id: String = ""
    var customer_phone: String = ""
    var customer_library: String = ""
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var count: Int = 0
    var init_count: Int = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var mMap: GoogleMap
    var latLngArrayList = ArrayList<com.google.android.gms.maps.model.LatLng>()
    lateinit var maker: Marker
    lateinit var polyline: Polyline
    val SMS_RECEIVE_PERMISSON = 99
    var polylineOptions=PolylineOptions().width(5f).color(Color.RED).clickable(true)
    val btnmHandler: Handler =  Handler()
    var check_thread:Boolean=true
    var startthread=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliveryman_showmap)
        startProcess()
        val locbundle = intent.getParcelableExtra<Bundle>("locbundle")
        if (locbundle != null) {
            locpoint = locbundle.getParcelable("libraryPoint")!!
            customerpoint = locbundle.getParcelable("customerPoint")!!
        }
        if (intent.hasExtra("customerid")) {
            customer_id = intent.getStringExtra("customerid").toString()
            Log.d("배달맵 메시지", customer_id)
        }
        val permissonCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)
        if (permissonCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "SMS 수신권한 있음", Toast.LENGTH_SHORT).show()
            btn_deliveryStartsend.setOnClickListener {
                access_database(customer_id, 1)

            }
            btn_deliveryArrivesend.setOnClickListener {
                access_database(customer_id, 2)
            }
        } else {
            Toast.makeText(getApplicationContext(), "SMS 수신권한 없음", Toast.LENGTH_SHORT).show()
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECEIVE_SMS
                )
            ) {
                ActivityCompat.requestPermissions(this, permissions, SMS_RECEIVE_PERMISSON);
            } else {
                ActivityCompat.requestPermissions(this, permissions, SMS_RECEIVE_PERMISSON)
            }
        }
        btn_deliveryComplete.setOnClickListener {
           db.collection("Orders").whereEqualTo("customerid",customer_id)
               .get().addOnSuccessListener { doc->
                   for(index in doc){
                       if(index.data["ordercomplete"].toString().toBoolean()){
                           db.collection("Orders").document(index.id).delete()
                           var initPage_intent= Intent(this, DeliveryOrderList::class.java)
                           startActivity(initPage_intent)

                       }
                       else{
                           Toast.makeText(getApplicationContext(), "고객 받기완료할 떄까지 기달려주세요", Toast.LENGTH_LONG).show()
                       }

                   }

               }
        }
        btnUpdateroad.setOnClickListener {
            Log.d("메시지", "경로 최신화 버튼 클릭")
            mMap.addPolyline(polylineOptions)
            //polyline=
        }
    }
    override fun onBackPressed() {
        //super.onBackPressed()
    }
    fun startProcess() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.deliverymapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    fun access_database(id: String, check: Int) {
        db.collection("customers").whereEqualTo("id", id)
            .get().addOnSuccessListener { doc ->
                for (index in doc) {
                    customer_phone = index.data["phone"].toString()
                    customer_library = index.data["selectLib"].toString()
                }
                //배달원 아이디 저장
//                db.collection("Orders").whereEqualTo("customerid",id)
//                    .get().addOnSuccessListener {doc ->
//                        for (index in doc) {
//                            db.collection("Orders").document(index.id).update("deliveryid",)
//                        }
//                    }
                try {
                    val sms = SmsManager.getDefault()

                    val phones = customer_phone

                    if (check == 1) {
                        sms.sendTextMessage(phones, null, "출발했습니다.", null, null);
                    } else if (check == 2) {
                        sms.sendTextMessage(phones, null, "도착하기 3분 전 입니다.", null, null);
                    }
                    Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show()

                } catch (e: Exception) {
                    Toast.makeText(getApplicationContext(), "전송 오류!", Toast.LENGTH_LONG).show()
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG)
                        .show()
                    e.printStackTrace();
                }
            }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("메시지", "onMapReady")
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(customerpoint).title(customer_id).icon(BitmapDescriptorFactory.fromResource(R.drawable.userimg)))
        mMap.addMarker(MarkerOptions().position(locpoint).title(customer_library).icon(BitmapDescriptorFactory.fromResource(R.drawable.libraryimg)))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        updateLoaction()

        var firststart=true
        thread(start=true) {
            Thread.sleep(1000)
            while (true) {
                if(startthread) {
//                    if(!firststart) {
//                        polyline.remove()
//                    }
                    val polylineOptions2=PolylineOptions().width(5f).color(Color.RED).clickable(true)
                    latLngArrayList.clear()
                    Log.d("메시지", "스레드 실행")
                    val urlString = setUri()
                    val values: ContentValues? = null
                    var result: String = ""
                    val relayTmapAPI = RelayTmapAPI()
                    result = relayTmapAPI.request(urlString, values).toString()
                    Log.d("메시지", "tmap 결과 값 저장")
                    val root = JSONObject(result)
                    //총 경로 횟수 featuresArray에 저장
                    val featuresArray = root.getJSONArray("features")
                    Log.d("메시지", "featuresArray 길이" + featuresArray.length())
                    for (i in 0 until featuresArray.length()) {
                        val featuresIndex = featuresArray[i] as JSONObject
                        val geometry = featuresIndex.getJSONObject("geometry")
                        val type = geometry.getString("type")
                        if (type == "LineString") {
                            val coordinatesArray = geometry.getJSONArray("coordinates")
                            for (j in 0 until coordinatesArray.length()) {
                                val pointArray = coordinatesArray[j] as JSONArray
                                val longitude = pointArray[0].toString().toDouble()
                                val latitude = pointArray[1].toString().toDouble()
                                latLngArrayList.add(
                                    com.google.android.gms.maps.model.LatLng(
                                        latitude,
                                        longitude
                                    )
                                )
                                Log.d("메시지", "좌표 :" + longitude + " " + latitude)
                            }
                        }
                    }
                    polylineOptions2.addAll(latLngArrayList)

                    Thread.sleep(2000)
                    runOnUiThread {
                        Log.d("메시지", "강제클릭 실행 전")
                        polylineOptions=polylineOptions2
                        btnUpdateroad.performClick()
                        firststart=false
                    }
                    Thread.sleep(300)
                    Log.d("메시지", "경로정보 저장")
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun updateLoaction() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create()
        locationRequest.run {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?.let {
                    for ((i, location) in it.locations.withIndex()) {
                        setLastLocation(location)
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }
    fun init_Polyoptions(p:PolylineOptions){


    }
    fun setLastLocation(lastLocation: Location) {
        if (count != 0) maker.remove()
        val LatLNG = LatLng(lastLocation.latitude, lastLocation.longitude)
        deliverypoint=LatLNG
        val markerOptions = MarkerOptions().position(LatLNG).title("내 위치")
        val cameraPosition = CameraPosition.builder().target(LatLNG).zoom(15.0f).build()
        //val cameraPosition = CameraPosition.builder().target(LatLNG).zoom(20.0f).build()
        maker = mMap.addMarker(markerOptions)
        maker.rotation
        maker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.deliveryimg))
        //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(LatLNG))
        deliverypoint = LatLNG
        if (init_count == 0) {
            //mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            val initLatLNG = LatLng(34.791823, 126.365268)
            val initcameraPosition = CameraPosition.builder().target(initLatLNG).zoom(15.0f).build()
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(initcameraPosition))
        }
        init_count++;
        count++
    }
    fun setUri(): String {
        val startName=URLEncoder.encode("출발지","UTF-8")
        val endName=URLEncoder.encode("도착지","UTF-8")
        val urlString =
            "https://apis.openapi.sk.com/tmap/routes?version=1&format=json&callback=result&appKey=l7xx7c351c7720cb4c108b5c3f5cd5538575+&startX=" +
                    deliverypoint.longitude+"&startY="+ deliverypoint.latitude + "&endX=" + locpoint.longitude + "&endY=" + locpoint.latitude
        //+"&startName="+startName+"&endName="+endName
        return urlString
    }
}


