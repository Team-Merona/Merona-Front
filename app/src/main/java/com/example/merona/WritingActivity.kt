package com.example.merona

import android.annotation.SuppressLint
import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_modify.*

import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_writing.*
import kotlinx.android.synthetic.main.check_dialog.*
import org.json.JSONObject

class WritingActivity : AppCompatActivity() {
    val writingUrl = "http://10.0.2.2:8080/board/save"
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_writing)

        PostButton.isEnabled = true

        //뒤로가기 버튼 만들기
        val toolbar = findViewById<Toolbar>(R.id.back_home)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.left_arrow)


        //가입하기 버튼 클릭 시
        PostButton.setOnClickListener {
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, writingUrl,
                Response.Listener {
                    Log.d("게시글 작성 성공", inputTitle.text.toString())
                    onBackPressed()
                },
                Response.ErrorListener {
                    Log.d("error",it.toString())
                }
            ){
                override fun getBody(): ByteArray {
                    val json = JSONObject()
                    json.put("title", ""+inputTitle.text.toString())
                    json.put("contents", ""+inputContents.text.toString())
                    json.put("streetAddress",""+address.text.toString())
                    return json.toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json; charset=utf-8"
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["Content-Type"] = "application/json"
                    headerMap["Authorization"] = "Bearer "+MyApplication.prefs.getString("accessToken","")
                    return headerMap
                }
            }
            val queue = Volley.newRequestQueue(this)
            queue.add(stringRequest)
        }

    }
}