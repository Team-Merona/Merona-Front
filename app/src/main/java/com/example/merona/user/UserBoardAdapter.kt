package com.example.merona.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.merona.R
import com.example.merona.board.BoardDetailActivity

class UserBoardAdapter(val itemList: ArrayList<UserBoardItem>) :
    RecyclerView.Adapter<UserBoardAdapter.BoardViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mypage_recyclerview, parent, false)
            return BoardViewHolder(view)
        }

        override fun onBindViewHolder(holder: BoardViewHolder, position: Int){
            holder.tv_title.text = itemList[position].title
            holder.btn_state.text = itemList[position].state
            if (holder.btn_state.text=="완료"){
                holder.btn_state.setBackgroundResource(R.drawable.button_solid_gray)
                holder.btn_state.isEnabled = false
            }
            else if(holder.btn_state.text=="대기중"){
                holder.btn_state.isEnabled = false
            }
            else if(holder.btn_state.text=="진행중"){
                holder.btn_state.isEnabled = true
            }

            holder.btn_state.setOnClickListener{
                val broadcaster = LocalBroadcastManager.getInstance(it.context)
                val intent = Intent("stateChange")
                intent.putExtra("id",itemList[position].id)
                broadcaster.sendBroadcast(intent)
                holder.btn_state.text = "완료"
                holder.btn_state.setBackgroundResource(R.drawable.button_solid_gray)
                holder.btn_state.isEnabled = false
            }

            holder.tv_title.setOnClickListener {
                val intent = Intent(it.context, BoardDetailActivity::class.java)
                intent.putExtra("id", itemList[position].id)
                it.context.startActivity(intent)
            }
        }

        override fun getItemCount():Int{
            return itemList.count()
        }

        inner class BoardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val tv_title = itemView.findViewById<TextView>(R.id.tv_title)
            val btn_state = itemView.findViewById<Button>(R.id.btn_state)
        }
}