package com.example.reigster_show
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reigster_show.databinding.ActivityDeliveryorderlistBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.orderitem_recycler.view.*
import kotlinx.android.synthetic.main.activity_deliveryorderlist.*


class DeliveryOrderList : AppCompatActivity() {

//    val binding = ActivityDeliveryorderlistBinding.inflate(layoutInflater)
//    var OrderList = arrayListOf<Orders>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deliveryorderlist)
        val adpater = OrdersAdapter()

        recyclerView.adapter = adpater
        Log.d("메시지", "binding.recyclerView.adapter = orderapdater 실행")
        recyclerView.layoutManager = LinearLayoutManager(this)
        Log.d("메시지", "binding.recyclerView.layoutManager = LinearLayoutManager(this) 실행")
//        btnOrderload.setOnClickListener {
//            db.collection("Orders").get().addOnSuccessListener { result ->
//                OrderList.clear()
//                Log.d("메시지", "db 불러오는 중")
//                for (index in result) {
//                    Log.d("메시지", "db 반복문 시작")
//                    var selectOrders = index.toObject(Orders::class.java)
//                    OrderList.add(selectOrders)
//                }
//                Log.d("메시지", "db 불러오는 중")
//                adpater.notifyDataSetChanged()
//            }
//        }
    }
}
     class OrdersAdapter : RecyclerView.Adapter<OrdersAdapter.OrderHolder>() {
         var OrderList = arrayListOf<Orders>()
         var db: FirebaseFirestore = FirebaseFirestore.getInstance()
         init {
             Log.d("메시지", "어답터 init 중")
             db.collection("Orders").get().addOnSuccessListener { result ->
                 OrderList.clear()
                 Log.d("메시지", "db 불러오는 중")
                 for (index in result) {
                     Log.d("메시지", "db 반복문 시작")
                     var selectOrders = index.toObject(Orders::class.java)
                     OrderList.add(selectOrders)
                 }
                 Log.d("메시지", "db 불러오는 중")
                 notifyDataSetChanged()
             }


         }
         override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHolder {
             Log.d("메시지", "onCreateViewHolder 메소드 입장")
             val views = LayoutInflater.from(parent.context).inflate(R.layout.orderitem_recycler,parent,false)
             return OrderHolder(views)
         }

         override fun onBindViewHolder(holder: OrderHolder, position: Int) {
             Log.d("메시지", "onBindViewHolder 메소드 입장")
             var viewHolder = (holder as OrderHolder).itemView
             viewHolder.textOrderCustomId.text = OrderList.get(position).customerid
             Log.d("메시지", viewHolder.textOrderCustomId.text.toString())
             viewHolder.textorderaddress.text = OrderList.get(position).orderaddress
             Log.d("메시지", viewHolder.textorderaddress.text.toString())
             viewHolder.textorderlibrary.text = OrderList.get(position).orderlib
             Log.d("메시지", viewHolder.textorderlibrary.text.toString())
             holder.itemView.setOnClickListener {
                 val orderPage_intent:Intent = Intent(holder.itemView?.context, OrderdetailPage::class.java)
                 orderPage_intent.putExtra("customer_id",OrderList.get(position).customerid)
                 ContextCompat.startActivity(holder.itemView.context,orderPage_intent,null)
             }
         }

         override fun getItemCount(): Int {
             Log.d("메시지", OrderList.size.toString())
             return OrderList.size
         }

         class OrderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         }

    }


