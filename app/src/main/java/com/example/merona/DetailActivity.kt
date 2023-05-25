package com.example.merona

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONArray
import org.json.JSONObject

class DetailActivity : AppCompatActivity() {
    val boardDetailUrl = "http://10.0.2.2:8080/board/list/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val boardId = intent.getLongExtra("id",0)
        val request=object: StringRequest(
            Request.Method.GET,
            boardDetailUrl+boardId.toString(),
            Response.Listener<String>{ response ->
                Log.d("응답!",response)
                var strResp = response.toString()
                val jsonObj: JSONObject = JSONObject(strResp)
                val id = jsonObj.getLong("id")
                val title = jsonObj.getString("title")
                val contents = jsonObj.getString("contents")
                val address = jsonObj.getString("address")
//                    val cost = jsonObject.getString("cost")
                val email = jsonObj.getString("email")

                tvName.text = email
                tvTitle.text = title
                tvContents.text = contents
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

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

}