package com.example.merona.board

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.util.MyApplication
import com.example.merona.R
import org.json.JSONArray
import java.io.UnsupportedEncodingException


class BoardListFragment : Fragment() {

//    private var boardlistUrl = "http://3.36.142.103:8080/board/list"
    private var boardlistUrl = "http://10.0.2.2:8080/board/list"
//    private var boardlistUrl = "http://172.30.1.5:8080/board/list"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingInflatedId")
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

                    val id = jsonObject.getLong("id")
                    val title = jsonObject.getString("title")
                    val addressJsonObject = jsonObject.getJSONObject("address")
                    val address = addressJsonObject.getString("streetAddress")
                    val cost = jsonObject.getInt("cost")

                    itemList.add(BoardItem(id,title,address,cost.toString()+"원"))
                }

                Log.d("저장!", itemList.toString())
                boardAdapter.notifyDataSetChanged()

            },
            {
                Log.d("에러!","x..")
            }

        ){
            //response를 UTF8로 변경해주는 소스코드
            override fun parseNetworkResponse(response: NetworkResponse): Response<String?>? {
                return try {
                    val utf8String = String(response.data, Charsets.UTF_8)
                    Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: UnsupportedEncodingException) {
                    // log error
                    Response.error(ParseError(e))
                } catch (e: Exception) {
                    // log error
                    Response.error(ParseError(e))
                }
            }

            override fun getParams():MutableMap<String,String>{
                val params=HashMap<String,String>()
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["Content-Type"] = "application/json"
                headerMap["Authorization"] = "Bearer "+ MyApplication.prefs.getString("accessToken","")
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

                        val id = jsonObject.getLong("id")
                        val title = jsonObject.getString("title")
                        val addressJsonObject = jsonObject.getJSONObject("address")
                        val address = addressJsonObject.getString("streetAddress")
                        val cost = jsonObject.getInt("cost")

                        itemList.add(BoardItem(id,title,address,cost.toString()+"원"))
                    }

                    Log.d("저장!", itemList.toString())
                    boardAdapter.notifyDataSetChanged()

                },
                {
                    Log.d("에러!","x..")
                }

            ){
                //response를 UTF8로 변경해주는 소스코드
                override fun parseNetworkResponse(response: NetworkResponse): Response<String?>? {
                    return try {
                        val utf8String = String(response.data, Charsets.UTF_8)
                        Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response))
                    } catch (e: UnsupportedEncodingException) {
                        // log error
                        Response.error(ParseError(e))
                    } catch (e: Exception) {
                        // log error
                        Response.error(ParseError(e))
                    }
                }

                override fun getParams():MutableMap<String,String>{
                    val params=HashMap<String,String>()
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["Content-Type"] = "application/json"
                    headerMap["Authorization"] = "Bearer "+ MyApplication.prefs.getString("accessToken","")
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