package com.example.merona.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.merona.util.MyApplication
import com.example.merona.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {
    companion object{
        fun newInstance() : ChatFragment {
            return ChatFragment()
        }
    }
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var recyclerView : RecyclerView? = null

    //메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    //프레그먼트를 포함하고 있는 액티비티에 붙었을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById<RecyclerView>(R.id.chatfragment_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(requireContext())
        recyclerView!!.adapter = RecyclerViewAdapter()
        Log.d("meessage fragment", "됨!")

        return view
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        private val chatModel = ArrayList<ChatModel>()
        private var uid : String? = null
        private val destinationUsers : ArrayList<String> = arrayListOf()
        private val boardIdArray : ArrayList<Long> = arrayListOf()
        init {
            uid = MyApplication.prefs.getString("email", "")
            println(uid)

            fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatModel.clear()
                    for(data in snapshot.children){
                        chatModel.add(data.getValue<ChatModel>()!!)
                        println(data)
                    }
                    notifyDataSetChanged()
                }
            })
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_recyclerview, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView_title : TextView = itemView.findViewById(R.id.chat_textview_title)
            val textView_lastMessage : TextView = itemView.findViewById(R.id.chat_item_textview_lastmessage)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            var destinationUid: String? = null

            //채팅방에 있는 유저 모두 체크
            for (user in chatModel[position].users.keys) {
                if (!user.equals(uid)) {
                    destinationUid = user
                    destinationUsers.add(destinationUid)
                }
            }
            //boardIdArray에 boardId들을 add시켜야함
            var boardId : Long? = null
            for(board in chatModel[position].boardId.keys) {
                boardId = board.toLong()
                boardIdArray.add(boardId)
            }
            fireDatabase.child("users").child("$destinationUid").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    holder.textView_title.text = destinationUid
                }
            })
            //메세지 내림차순 정렬 후 마지막 메세지의 키값을 가져옴
            val commentMap = TreeMap<String, ChatModel.Comment>(Collections.reverseOrder())
            commentMap.putAll(chatModel[position].comments)
            val lastMessageKey = commentMap.keys.toTypedArray()[0]
            holder.textView_lastMessage.text = chatModel[position].comments[lastMessageKey]?.message

            //채팅창 선택 시 이동
            //클릭 된 채팅창의 destinationUId와 boardId를 넘겨야함
            holder.itemView.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("destinationUId", destinationUsers[position])
                 intent.putExtra("boardId", boardIdArray[position])
                context?.startActivity(intent)
            }
        }
        override fun getItemCount(): Int {
            return chatModel.size
        }
    }
}