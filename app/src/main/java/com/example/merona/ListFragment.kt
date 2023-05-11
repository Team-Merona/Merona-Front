package com.example.merona

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray

class ListFragment : Fragment() {

    private var boardlistUrl = "http://10.0.2.2:8080/board/list"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_list, container, false)

        // broadcast 등록
        register()

        val rvBoard = view.findViewById<RecyclerView>(R.id.rv_board)
        val itemList = ArrayList<BoardItem>()

        val boardAdapter = BoardAdapter(itemList)
        boardAdapter.notifyDataSetChanged()

        rvBoard.adapter = boardAdapter
        rvBoard.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val request=object: StringRequest(
            Request.Method.GET,
            boardlistUrl,
            Response.Listener<String>{ response ->
                Log.d("응답!",response)
                var strResp = response.toString()
                val jsonArray = JSONArray(strResp)
                for (i in 0..jsonArray.length()-1){
                    val jsonObject = jsonArray.getJSONObject(i)

                    val title = jsonObject.getString("title")
                    val address = jsonObject.getString("address")
//                    val cost = jsonObject.getString("cost")

                    itemList.add(BoardItem(title,address,"1000"+"원"))
                }

                Log.d("저장!", itemList.toString())
                boardAdapter.notifyDataSetChanged()

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

        val queue = Volley.newRequestQueue(context)
        queue.add(request)

        return view
    }

    private fun register() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            boardReceiver , IntentFilter("Board")
        )
    }

    fun unRegister() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(boardReceiver)
    }

    private val boardReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("boardReceiver", "Intent: $intent")
            val rvBoard = view!!.findViewById<RecyclerView>(R.id.rv_board)
            val itemList = ArrayList<BoardItem>()

            val boardAdapter = BoardAdapter(itemList)
            boardAdapter.notifyDataSetChanged()

            rvBoard.adapter = boardAdapter
            rvBoard.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val request=object: StringRequest(
                Request.Method.GET,
                boardlistUrl,
                Response.Listener<String>{ response ->
                    Log.d("응답!",response)
                    var strResp = response.toString()
                    val jsonArray = JSONArray(strResp)
                    for (i in 0..jsonArray.length()-1){
                        val jsonObject = jsonArray.getJSONObject(i)

                        val title = jsonObject.getString("title")
                        val address = jsonObject.getString("address")
//                    val cost = jsonObject.getString("cost")

                        itemList.add(BoardItem(title,address,"1000"+"원"))
                    }

                    Log.d("저장!", itemList.toString())
                    boardAdapter.notifyDataSetChanged()

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

            val queue = Volley.newRequestQueue(context)
            queue.add(request)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegister()
    }


}