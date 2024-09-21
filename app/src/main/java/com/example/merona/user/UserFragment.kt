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
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.R
import com.example.merona.board.BoardState
import com.example.merona.databinding.FragmentUserBinding
import com.example.merona.util.MyApplication
import org.json.JSONArray
import java.io.UnsupportedEncodingException

class UserFragment : Fragment() {
    private var binding: FragmentUserBinding? = null

    private var boardUrl = "/user/list"
    private var stateUrl = "/board/list/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)

        // broadcast 등록
        broadcastRegister()

        setScreen()
        setClickListener()

        return binding!!.root
    }

    private fun broadcastRegister() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            profileReceiver, IntentFilter("profile")
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            boardReceiver, IntentFilter("userBoard")
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            stateReceiver, IntentFilter("stateChange")
        )
    }

    private fun broadcastUnregister() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(profileReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(boardReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(stateReceiver)
    }

    private val profileReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("profileReceiver", "Intent: $intent")
            Log.d("email 저장", MyApplication.prefs.getString("email", ""))
            binding!!.userEmail.text = MyApplication.prefs.getString("email", "") + " 님"
        }
    }

    private val boardReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("boardReceiver", "Intent: $intent")
            val itemList = ArrayList<UserBoardItem>()

            val boardAdapter = UserBoardAdapter(itemList)
            boardAdapter.notifyDataSetChanged()

            binding!!.rvUserBoard.adapter = boardAdapter
            binding!!.rvUserBoard.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val request = object : StringRequest(
                Method.GET,
                getString(R.string.prefix_uri) + boardUrl,
                Response.Listener<String> { response ->
                    Log.d("응답!", response)
                    var strResp = response.toString()
                    val jsonArray = JSONArray(strResp)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val userBoardItem = UserBoardItem(
                            jsonObject.getLong("id"),
                            jsonObject.getString("title"),
                            BoardState.valueOf(jsonObject.getString("state")).getKorState()
                        )
                        itemList.add(userBoardItem)
                    }
                    Log.d("저장!", itemList.toString())
                    boardAdapter.notifyDataSetChanged()
                },
                {
                    Log.d("에러!", it.message!!)
                }

            ) {
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

                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["Content-Type"] = "application/json"
                    headerMap["Authorization"] =
                        "Bearer " + MyApplication.prefs.getString("accessToken", "")
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
            val boardId = intent.getLongExtra("id", 0)

            val itemList = ArrayList<UserBoardItem>()

            val boardAdapter = UserBoardAdapter(itemList)
            boardAdapter.notifyDataSetChanged()

            binding!!.rvUserBoard.adapter = boardAdapter
            binding!!.rvUserBoard.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val request = object : StringRequest(
                Method.PATCH,
                getString(R.string.prefix_uri) + "$stateUrl$boardId/completed",
                Response.Listener<String> { response ->
                    Log.d("응답!", response)
                    val broadcaster = LocalBroadcastManager.getInstance(context)
                    val intent = Intent("userBoard")
                    broadcaster.sendBroadcast(intent)
                },
                {
                    Log.d("에러!", it.message!!)
                }

            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    return params
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["Content-Type"] = "application/json"
                    headerMap["Authorization"] =
                        "Bearer " + MyApplication.prefs.getString("accessToken", "")
                    return headerMap
                }
            }

            val queue = Volley.newRequestQueue(context)
            queue.add(request)
        }
    }

    private fun setClickListener() {
        binding!!.modifyBtn.setOnClickListener {
            val intent = Intent(activity, UserModifyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setScreen() {
        Log.d("email 저장", MyApplication.prefs.getString("email", ""))
        binding!!.userEmail.text = MyApplication.prefs.getString("email", "") + " 님"

        val itemList = ArrayList<UserBoardItem>()

        val boardAdapter = UserBoardAdapter(itemList)
        boardAdapter.notifyDataSetChanged()

        binding!!.rvUserBoard.adapter = boardAdapter
        binding!!.rvUserBoard.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val request = object : StringRequest(
            Method.GET,
            getString(R.string.prefix_uri) + boardUrl,
            Response.Listener<String> { response ->
                Log.d("응답!", response)
                var strResp = response.toString()
                val jsonArray = JSONArray(strResp)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val userBoardItem = UserBoardItem(
                        jsonObject.getLong("id"),
                        jsonObject.getString("title"),
                        BoardState.valueOf(jsonObject.getString("state")).getKorState()
                    )
                    itemList.add(userBoardItem)
                }
                Log.d("저장!", itemList.toString())
                boardAdapter.notifyDataSetChanged()
            },
            {
                Log.d("에러!", it.message!!)
            }

        ) {
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

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["Content-Type"] = "application/json"
                headerMap["Authorization"] =
                    "Bearer " + MyApplication.prefs.getString("accessToken", "")
                return headerMap
            }
        }

        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastUnregister()
    }
}