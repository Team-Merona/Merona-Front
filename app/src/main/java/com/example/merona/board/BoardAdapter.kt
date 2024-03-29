package com.example.merona.board

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.merona.R

class BoardAdapter(val itemList: ArrayList<BoardItem>) :
    RecyclerView.Adapter<BoardAdapter.BoardViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_board_recyclerview, parent, false)
        return BoardViewHolder(view)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int){
        holder.tv_title.text = itemList[position].title
        holder.tv_address.text = itemList[position].address
        holder.tv_cost.text= itemList[position].cost

        holder.itemView.setOnClickListener{
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
        val tv_address = itemView.findViewById<TextView>(R.id.tv_address)
        val tv_cost = itemView.findViewById<TextView>(R.id.tv_cost)

    }
}