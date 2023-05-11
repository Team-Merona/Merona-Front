package com.example.merona

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.NonDisposableHandle.parent
import org.json.JSONArray

class UserBoardAdapter(val itemList: ArrayList<UserBoardItem>) :
    RecyclerView.Adapter<UserBoardAdapter.BoardViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mypage_recyclerview, parent, false)
            return BoardViewHolder(view)
        }

        override fun onBindViewHolder(holder: BoardViewHolder, position: Int){
            holder.tv_title.text = itemList[position].title
            holder.btn_state.text = itemList[position].state

        }

        override fun getItemCount():Int{
            return itemList.count()
        }

        inner class BoardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            val tv_title = itemView.findViewById<TextView>(R.id.tv_title)
            val btn_state = itemView.findViewById<Button>(R.id.btn_state)
        }
}