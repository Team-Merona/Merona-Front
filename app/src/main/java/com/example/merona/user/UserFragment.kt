package com.example.merona.user

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.util.MyApplication
import com.example.merona.R
import org.json.JSONArray
import java.io.UnsupportedEncodingException


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class UserFragment : Fragment() {

    private var boardUrl = "http://10.0.2.2:8080/user/list"
    private var stateUrl = "http://10.0.2.2:8080/board/list/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_user, container, false)

        // broadcast 등록
        register()

        val modifyButton : AppCompatButton = view.findViewById(R.id.btn_modify)
        modifyButton.setOnClickListener {
            val intent = Intent(getActivity(), UserModifyActivity::class.java)
            startActivity(intent)
        }

        Log.d("email 저장", MyApplication.prefs.getString("email", ""))
        val userEmail : TextView = view.findViewById(R.id.userEmail)
        userEmail.text = MyApplication.prefs.getString("email","")+" 님"

        val rvBoard = view.findViewById<RecyclerView>(R.id.rv_user_board)
        val itemList = ArrayList<UserBoardItem>()

        val boardAdapter = UserBoardAdapter(itemList)
        boardAdapter.notifyDataSetChanged()

        rvBoard.adapter = boardAdapter
        rvBoard.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val request=object: StringRequest(
            Method.GET,
            boardUrl,
            Response.Listener<String>{ response ->
                Log.d("응답!", response)
                var strResp = response.toString()
                val jsonArray = JSONArray(strResp)
                for (i in 0..jsonArray.length()-1){
                    val jsonObject = jsonArray.getJSONObject(i)
                    val id = jsonObject.getLong("id")
                    val title = jsonObject.getString("title")

                    var state = jsonObject.getString("state")
                    if (state=="REQUEST_WAITING"){
                        state = "대기중"
                    }
                    else if(state =="REQUEST_ON_GOING"){
                        state = "진행중"
                    }
                    else{
                        state="완료"
                    }

                    itemList.add(UserBoardItem(id, title, state))
                }

                Log.d("저장!", itemList.toString())
                boardAdapter.notifyDataSetChanged()

            },
            {
                Log.d("에러!", "x..")
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
            profileReceiver , IntentFilter("profile")
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            boardReceiver , IntentFilter("userBoard")
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            stateReceiver , IntentFilter("stateChange")
        )
    }

    fun unRegister() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(profileReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(boardReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(stateReceiver)
    }

    private val profileReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("profileReceiver", "Intent: $intent")
            Log.d("email 저장", MyApplication.prefs.getString("email", ""))
            val userEmail : TextView = view!!.findViewById(R.id.userEmail)
            userEmail.text = MyApplication.prefs.getString("email","")+" 님"
        }
    }

    private val boardReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("boardReceiver", "Intent: $intent")
            val rvBoard = view!!.findViewById<RecyclerView>(R.id.rv_user_board)
            val itemList = ArrayList<UserBoardItem>()

            val boardAdapter = UserBoardAdapter(itemList)
            boardAdapter.notifyDataSetChanged()

            rvBoard.adapter = boardAdapter
            rvBoard.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val request=object: StringRequest(
                Method.GET,
                boardUrl,
                Response.Listener<String>{ response ->
                    Log.d("응답!", response)
                    var strResp = response.toString()
                    val jsonArray = JSONArray(strResp)
                    for (i in 0..jsonArray.length()-1){
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getLong("id")
                        val title = jsonObject.getString("title")

                        var state = jsonObject.getString("state")
                        if (state=="REQUEST_WAITING"){
                            state = "대기중"
                        }
                        else if(state =="REQUEST_ON_GOING"){
                            state = "진행중"
                        }
                        else{
                            state="완료"
                        }

                        itemList.add(UserBoardItem(id, title, state))
                    }

                    Log.d("저장!", itemList.toString())
                    boardAdapter.notifyDataSetChanged()

                },
                {
                    Log.d("에러!", "x..")
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

    private val stateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("stateReceiver", "Intent: $intent")
            val boardId = intent.getLongExtra("id",0)

            val rvBoard = view!!.findViewById<RecyclerView>(R.id.rv_user_board)
            val itemList = ArrayList<UserBoardItem>()

            val boardAdapter = UserBoardAdapter(itemList)
            boardAdapter.notifyDataSetChanged()

            rvBoard.adapter = boardAdapter
            rvBoard.layoutManager= LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val request=object: StringRequest(
                Method.PATCH,
                "$stateUrl$boardId/completed",
                Response.Listener<String>{ response ->
                    Log.d("응답!", response)
                    val broadcaster = LocalBroadcastManager.getInstance(context)
                    val intent2 = Intent("userBoard")
                    broadcaster.sendBroadcast(intent2)
                },
                {
                    Log.d("에러!", "x..")
                }

            ){
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