package com.example.merona

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_modify.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.check_dialog.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    val url = "http://10.0.2.2:8080/user/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //회원 가입 버튼 클릭 시 화면 전환
        move_btn.setOnClickListener{
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        //로그인 버튼 클릭 시 지도 화면 전환
        login_btn.setOnClickListener{
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener {
                    Log.d("로그인 성공", editTextEmailAddress.text.toString())
                    var strResp = it.toString()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val accessToken = jsonObj.getString("accessToken")
                    MyApplication.prefs.setString("accessToken", accessToken)
                    MyApplication.prefs.setString("email", editTextEmailAddress.text.toString())
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)},
                Response.ErrorListener {
                    Log.d("로그인 실패", it.toString())
                    val dlgPopup = ConfirmDialog(this,"로그인에 실패하였습니다.")
                    dlgPopup.show()
                    dlgPopup.window!!.setLayout(800,450)
                    dlgPopup.setCancelable(false)
                    dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
                }) {

                override fun getBody(): ByteArray {
                    val json = JSONObject()
                    json.put("email", ""+editTextEmailAddress.text.toString())
                    json.put("password", ""+editTextPassword.text.toString())
                    return json.toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json; charset=utf-8"
                }
            }

            val queue = Volley.newRequestQueue(this)
            queue.add(stringRequest)
        }
    }
}