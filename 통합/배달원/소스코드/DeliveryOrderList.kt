package com.example.reigster_show
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.ViewGroup
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.FtsOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.orderitem_recycler.view.*
import kotlinx.android.synthetic.main.activity_deliveryorderlist.*
import kotlin.concurrent.thread


class DeliveryOrderList : AppCompatActivity() {

    //    val binding = ActivityDeliveryorderlistBinding.inflate(layoutInflater)
//    var OrderList = arrayListOf<Orders>()
    var threadstart: Int = 0
    var tempList = ArrayList<Orders>()
    var startthread:Int=0
    var Ddb: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliveryorderlist)
        threadstart = 0
        val adpater = OrdersAdapter()

        recyclerView.adapter = adpater
        Log.d("메시지", "binding.recyclerView.adapter = orderapdater 실행")
        recyclerView.layoutManager = LinearLayoutManager(this)
        startthread=adpater.clearthread
        val handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (startthread == 1) {
                    Log.d("메시지", "주문 목록 핸들러 정지")

                }
                else{
                    Log.d("메시지", "주문 목록 핸들러: " + threadstart)
                    Log.d("메시지", "주문 목록 핸들러 실행")
                    Ddb.collection("Orders").get().addOnSuccessListener { result ->
                        tempList.clear()
                        Log.d("메시지", "핸들러 db 불러오는 중")
                        for (index in result) {
                            Log.d("메시지", "핸들러  db 반복문 시작")
                            if (!index.data["orderCheck"].toString().toBoolean()) {
                                Log.d("메시지", "${index.data["customerid"]}: ${index.data["orderCheck"]}")
                                var selectOrders = index.toObject(Orders::class.java)
                                tempList.add(selectOrders)
                            }
                        }
                        adpater.updateReceiptsList(tempList)
                    }

//                val templist = adpater.OrderList
//                Log.d("메시지", "templist 크기: " + templist.size)
//                adpater.updateReceiptsList(templist)
                }
            }
        }

        Log.d("메시지", "binding.recyclerView.layoutManager = LinearLayoutManager(this) 실행")
        thread(start = true) {
            Log.d("메시지", "주문 목록  쓰레드 실행")
            startthread=adpater.clearthread
            while (true) {
                if(startthread != 1) {
                    Log.d("메시지", "주문 목록 쓰레드: " + startthread)
                    Thread.sleep(2000)
                    handler?.sendEmptyMessage(0)
                }
            }

        }

    }

    override fun onPause() {
        Log.d("메시지", "주문 목록 Pause")
        startthread=1
        super.onPause()
    }

    override fun onRestart() {
        Log.d("메시지", "주문 목록 Restart")
        startthread=0
        super.onRestart()
    }

    override fun onStop() {
        Log.d("메시지", "주문 목록 Stop")
        startthread=1
        super.onStop()
    }

}

    class OrdersAdapter : RecyclerView.Adapter<OrdersAdapter.OrderHolder>() {
        var OrderList = ArrayList<Orders>()
        var tmpList = arrayListOf<Orders>()
        var db: FirebaseFirestore = FirebaseFirestore.getInstance()
        var clearthread=0
        var dlist=DeliveryOrderList()

        init {
            Log.d("메시지", "어답터 init 중")
            db.collection("Orders").get().addOnSuccessListener { result ->
                OrderList.clear()
                Log.d("메시지", "db 불러오는 중")
                for (index in result) {
                    Log.d("메시지", "db 반복문 시작")
                    if (!index.data["orderCheck"].toString().toBoolean()) {
                        var selectOrders = index.toObject(Orders::class.java)
                        OrderList.add(selectOrders)
                    }
                }
                notifyDataSetChanged()
            }


        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHolder {
            Log.d("메시지", "onCreateViewHolder 메소드 입장")
            val views = LayoutInflater.from(parent.context)
                .inflate(R.layout.orderitem_recycler, parent, false)
            return OrderHolder(views)
        }

        override fun onBindViewHolder(holder: OrderHolder, position: Int) {
            Log.d("메시지", "onBindViewHolder 메소드 입장")
            if (!OrderList.get(position).orderCheck) {
                Log.d("메시지", OrderList.get(position).customerid+" "+OrderList.get(position).orderCheck.toString())
                var viewHolder = (holder as OrderHolder).itemView
                viewHolder.textOrderCustomId.text = OrderList.get(position).customerid
                //Log.d("메시지", viewHolder.textOrderCustomId.text.toString())
                viewHolder.textorderaddress.text = OrderList.get(position).orderaddress
                // Log.d("메시지", viewHolder.textorderaddress.text.toString())
                viewHolder.textorderlibrary.text = OrderList.get(position).orderlib
                //Log.d("메시지", viewHolder.textorderlibrary.text.toString())
                holder.itemView.setOnClickListener {
                    val orderPage_intent: Intent =
                        Intent(holder.itemView?.context, OrderdetailPage::class.java)
                    orderPage_intent.putExtra("customer_id", OrderList.get(position).customerid)
                    ContextCompat.startActivity(holder.itemView.context, orderPage_intent, null)
                    Log.d("메시지", "(주문 목록 핸들러) 아이템 클릭")
                    Log.d("메시지", "(주문 목록 핸들러) 아이템 클릭시" + DeliveryOrderList().threadstart)
                    clearthread=1
                    DeliveryOrderList().finish()
                }
            }
        }

        override fun getItemCount(): Int {
            Log.d("메시지","getItemCount: " +OrderList.size.toString())
            return OrderList.size
        }

         class OrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        }

        fun updateReceiptsList(newlist:ArrayList<Orders>) {
            OrderList.clear()
            OrderList.addAll(newlist)
            Log.d("메시지", "updateReceiptsList addAll 크기: "+OrderList.size)
            notifyDataSetChanged()
        }

    }


