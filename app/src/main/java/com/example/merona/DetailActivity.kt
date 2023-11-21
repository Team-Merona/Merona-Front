package com.example.merona

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class DetailActivity : AppCompatActivity() {
//    val boardDetailUrl = "http://3.36.142.103:8080/board/list/"
    val boardDetailUrl = "http://10.0.2.2:8080/board/list/"
    var email : String? = null //게시글 작성자의 ID

    private lateinit var naverMap: NaverMap
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
                val addressJsonObject = jsonObj.getJSONObject("address")
                val address = addressJsonObject.getString("streetAddress")
                val cost = jsonObj.getString("cost")
                var state = jsonObj.getString("state")
                email = jsonObj.getString("email")

                tvName.text = email+"님"
                if (email==MyApplication.prefs.getString("email","")){
//                    chat_btn.setBackgroundResource(R.drawable.rectangle_button)
//                    chat_btn.isEnabled = false
                }
                else{
                    chat_btn.setBackgroundResource(R.drawable.rectangle_button_79d4682)
                    chat_btn.isEnabled = true
                }
                tvTitle.text = title
                tvContents.text = contents
                tvCost.text = cost.toString()+"원"
                if (state=="REQUEST_WAITING"){
                    state = "요청 대기중"
                }
                else if(state =="REQUEST_ON_GOING"){
                    state = "요청 진행중"
                }
                else{
                    state="요청 완료"
                    requestBtn.setBackgroundResource(R.drawable.solid_button_gray)
                }
                requestBtn.text = state
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
                headerMap["Authorization"] = "Bearer "+MyApplication.prefs.getString("accessToken","")
                return headerMap
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)

        //채팅하기 버튼 클릭 시 채팅
        val chatBtn : AppCompatButton = findViewById(R.id.chat_btn)
        chatBtn.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            //destinationUId에 게시글 작성자의 ID를 넣음
            intent.putExtra("destinationUId", email)
            intent.putExtra("boardId", boardId)
            startActivity(intent)
        }



    }

}