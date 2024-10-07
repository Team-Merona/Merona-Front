package com.example.merona.user

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.merona.R
import com.example.merona.board.BoardDetailActivity
import com.example.merona.board.BoardState
import com.example.merona.databinding.ItemMypageRecyclerviewBinding

class UserBoardAdapter(private val itemList: ArrayList<UserBoardItem>) :
    RecyclerView.Adapter<UserBoardAdapter.BoardViewHolder>() {
    private var binding: ItemMypageRecyclerviewBinding? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = ItemMypageRecyclerviewBinding.inflate(inflater, parent, false)
        return BoardViewHolder(binding!!.root)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.title.text = itemList[position].title
        holder.stateBtn.text = itemList[position].state

        when (holder.stateBtn.text) {
            BoardState.REQUEST_COMPLETE.getKorState() -> {
                holder.stateBtn.setBackgroundResource(R.drawable.button_solid_gray)
                holder.stateBtn.isEnabled = false
            }

            BoardState.REQUEST_WAITING.getKorState() -> {
                holder.stateBtn.isEnabled = false
            }

            BoardState.REQUEST_ON_GOING.getKorState() -> {
                holder.stateBtn.isEnabled = true
            }
        }

        holder.stateBtn.setOnClickListener {
            val broadcaster = LocalBroadcastManager.getInstance(it.context)
            val intent = Intent("stateChange")
            intent.putExtra("id", itemList[position].id)
            broadcaster.sendBroadcast(intent)
            holder.stateBtn.text = BoardState.REQUEST_COMPLETE.getKorState()
            holder.stateBtn.setBackgroundResource(R.drawable.button_solid_gray)
            holder.stateBtn.isEnabled = false
        }

        holder.title.setOnClickListener {
            val intent = Intent(it.context, BoardDetailActivity::class.java)
            intent.putExtra("id", itemList[position].id)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return itemList.count()
    }

    inner class BoardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = binding!!.title
        val stateBtn = binding!!.stateBtn
    }
}