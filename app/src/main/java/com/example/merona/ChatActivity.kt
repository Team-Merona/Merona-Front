package com.example.merona

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.merona.ChatModel.Comment
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.ChatModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity() {
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid : String? = null
    private var chatRoomboardId : Long? = null   //게시글마다 채팅창을 새로 만들기 위해 사용
    private var destinationUid : String? = null
    private var boardId : Long? = null
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null
    //게시글 id 속성

//    val boardDetailUrl = "http://3.36.142.103:8080/board/list/"
//    private var stateUrl = "http://3.36.142.103:8080/board/list/"
    val boardDetailUrl = "http://192.168.45.7:8080/board/list/"
    private var stateUrl = "http://192.168.45.7:8080/board/list/"

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        //입력칸, 보내기 이미지
        val editText = findViewById<TextView>(R.id.messageActivity_editText)
        val imageView = findViewById<ImageView>(R.id.send_iv)

        //메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dataFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dataFormat.format(Date(time)).toString()

        destinationUid = intent.getStringExtra("destinationUId")
        boardId = intent.getLongExtra("boardId",0)

        //uid = 본인 uid
        //uid = Firebase.auth.currentUser?.uid.toString()
        uid = MyApplication.prefs.getString("email", "")
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        val request=object: StringRequest(
            Request.Method.GET,
            boardDetailUrl+boardId.toString(),
            Response.Listener<String>{ response ->
                Log.d("응답!",response)
                var strResp = response.toString()
                val jsonObj: JSONObject = JSONObject(strResp)
                val id = jsonObj.getLong("id")
                var state = jsonObj.getString("state")
                if (state=="REQUEST_WAITING"){
                    ongoingBtn.setBackgroundResource(R.drawable.solid_button_44be2d)
                    ongoingBtn.isEnabled = true
                }
                else{
                    ongoingBtn.setBackgroundResource(R.drawable.rectangle_button)
                    ongoingBtn.isEnabled = false
                }
            },
            {
                Log.d("에러!","x..")
            }

        ){
            override fun getParams():MutableMap<String,String>{
                val params=HashMap<String,String>()
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["Content-Type"] = "application/json"
                headerMap["Authorization"] = "Bearer "+MyApplication.prefs.getString("accessToken","")
                return headerMap
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)


        // 심부름 수락하기 버튼
        ongoingBtn.setOnClickListener{
            val request=object: StringRequest(
                Request.Method.PATCH,
                "$stateUrl$boardId/ongoing",
                Response.Listener<String>{ response ->
                    Log.d("응답!",response)
                    ongoingBtn.setBackgroundResource(R.drawable.rectangle_button)
                    ongoingBtn.isEnabled = false
                },
                {
                    Log.d("에러!","x..")
                }

            ){
                override fun getParams():MutableMap<String,String>{
                    val params=HashMap<String,String>()
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["Content-Type"] = "application/json"
                    headerMap["Authorization"] = "Bearer "+MyApplication.prefs.getString("accessToken","")
                    return headerMap
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(request)
        }

        //send 이미지 클릭 시 메세지 보내기
        imageView.setOnClickListener{
            Log.d("chatAct_boardId", boardId.toString())
            Log.d("chatAct_destinationUId","$destinationUid")
            Log.d("chatAct_uid", "$uid")

            val chatModel = ChatModel()
            chatModel.users.put(uid.toString(), true)
            chatModel.users.put(destinationUid!!, true)
            chatModel.boardId.put(boardId.toString(), true)
            val comment = Comment(uid, editText.text.toString(), curTime)
            if(chatRoomUid == null && chatRoomboardId == null) {
                imageView.isEnabled = false
                fireDatabase.child("chatrooms").push().setValue(chatModel).addOnSuccessListener {
                    //채팅방 생성
                    checkChatRoom()

                    //메세지 보내기
                    Handler().postDelayed({
                        println(chatRoomUid)
                        fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                        messageActivity_editText.text = null
                    }, 1000L)
                    Log.d("chatUidNull dest", "$destinationUid")
                }

            } else {
                fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                messageActivity_editText.text = null
                Log.d("chatUidNotMull dest", "$destinationUid")
            }
        }
        checkChatRoom()

    }

    private fun checkChatRoom() {
        //실시간으로 앱 데이터를 업데이트
        Log.d("checkChatRoom",chatRoomUid.toString())
        fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("checkChatRoom data change",chatRoomUid.toString())
                    for (item in snapshot.children) {
                        println(item)
                        val chatModel = item.getValue<ChatModel>()
                        if(chatModel?.users!!.containsKey(destinationUid) &&chatModel?.boardId!!.containsKey(boardId.toString())) {
                            chatRoomUid = item.key
                            chatRoomboardId = boardId
                            send_iv.isEnabled = true
                            recyclerView?.layoutManager = LinearLayoutManager(this@ChatActivity)
                            recyclerView?.adapter = RecyclerViewAdpater()
                        }
                    }
                }
            })
    }

    inner class RecyclerViewAdpater : RecyclerView.Adapter<RecyclerViewAdpater.MessageViewHolder>() {
        private val comments = ArrayList<Comment>()
        init {
            fireDatabase.child("users").child(destinationUid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    messageActivity_textView_topName.text = destinationUid
                    getMessageList()
                }
            })
        }

        fun getMessageList() {
            Log.d("getMessageList",chatRoomUid.toString())
            fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for(data in snapshot.children) {
                        val item = data.getValue<Comment>()
                        comments.add(item!!)
                        println("comments 내용!"+comments)
                    }
                    notifyDataSetChanged()
                    //메세지를 보낼 시 화면을 맨 밑으로 내림
                    recyclerView?.scrollToPosition(comments.size - 1)
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
            Log.d("리사이클러뷰 만들었음~","!")
            return MessageViewHolder(view)
        }
        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            //본인 채팅일 경우
            Log.d("리사이클러뷰 comments posiotion",comments[position].uid.toString())
            if(comments[position].uid.equals(uid)) {
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            } else { //상대방 채팅
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT
            }
        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView_message : TextView = view.findViewById(R.id.messageItem_textView_message)
            val layout_destination: LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layout_main : LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textView_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }
}