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
import androidx.appcompat.widget.AppCompatButton
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonArray
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_writing.*
import kotlinx.android.synthetic.main.fragment_user.*
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var boardUrl = "http://10.0.2.2:8080/user/list"
    private var stateUrl = "http://10.0.2.2:8080/board/list/{id}/completed"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

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
            val intent = Intent(getActivity(), ModifyActivity::class.java)
            startActivity(intent)
        }

        Log.d("email 저장",MyApplication.prefs.getString("email","") )
        val userEmail : TextView = view.findViewById(R.id.userEmail)
        userEmail.text = MyApplication.prefs.getString("email","")

        val rvBoard = view.findViewById<RecyclerView>(R.id.rv_user_board)
        val itemList = ArrayList<UserBoardItem>()

        val boardAdapter = UserBoardAdapter(itemList)
        boardAdapter.notifyDataSetChanged()

        rvBoard.adapter = boardAdapter
        rvBoard.layoutManager=LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val request=object: StringRequest(
            Request.Method.GET,
            boardUrl,
            Response.Listener<String>{ response ->
                Log.d("응답!",response)
                var strResp = response.toString()
                val jsonArray = JSONArray(strResp)
                for (i in 0..jsonArray.length()-1){
                    val jsonObject = jsonArray.getJSONObject(i)

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

                    itemList.add(UserBoardItem(title,state))
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
            profileReceiver , IntentFilter("profile")
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            boardReceiver , IntentFilter("userBoard")
        )
    }

    fun unRegister() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(profileReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(boardReceiver)
    }

    private val profileReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("profileReceiver", "Intent: $intent")
            Log.d("email 저장",MyApplication.prefs.getString("email","") )
            val userEmail : TextView = view!!.findViewById(R.id.userEmail)
            userEmail.text = MyApplication.prefs.getString("email","")
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
            rvBoard.layoutManager=LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

            val request=object: StringRequest(
                Request.Method.GET,
                boardUrl,
                Response.Listener<String>{ response ->
                    Log.d("응답!",response)
                    var strResp = response.toString()
                    val jsonArray = JSONArray(strResp)
                    for (i in 0..jsonArray.length()-1){
                        val jsonObject = jsonArray.getJSONObject(i)

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

                        itemList.add(UserBoardItem(title,state))
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


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}