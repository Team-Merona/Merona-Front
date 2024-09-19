package com.example.merona.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.R
import com.example.merona.databinding.ActivityLoginBinding
import com.example.merona.dialog.ConfirmDialog
import com.example.merona.home.MainActivity
import com.example.merona.util.MyApplication
import com.google.gson.Gson
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private var mBinding: ActivityLoginBinding? = null
    private val binding get() = mBinding!!

    private val uri = "/user/login"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListener()
    }

    private fun setClickListener() {
        //회원 가입 버튼
        binding.registerBtn.setOnClickListener {
            val intent = Intent(this, UserRegisterActivity::class.java)
            startActivity(intent)
        }

        //로그인 버튼 클릭
        binding.loginBtn.setOnClickListener {
            val stringRequest: StringRequest = object : StringRequest(
                Method.POST,
                getString(R.string.prefix_uri) + uri,
                Response.Listener {
                    Log.d("로그인 성공", binding.emailAddress.text.toString())
                    val strResp = it.toString()
                    val jsonObj = JSONObject(strResp)
                    val accessToken = jsonObj.getString("accessToken")
                    MyApplication.prefs.setString("accessToken", accessToken)
                    MyApplication.prefs.setString("email", binding.emailAddress.text.toString())
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                },
                Response.ErrorListener {
                    Log.d("로그인 실패", it.toString())
                    ConfirmDialog(this,getString(R.string.error_login)).createDialog()
                }) {

                override fun getBody(): ByteArray {
                    val loginRequest = LoginRequest(
                        binding.emailAddress.text.toString(),
                        binding.password.text.toString()
                    )
                    val json = Gson().toJson(loginRequest)
                    return json.toString().toByteArray()
                }

                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            val queue = Volley.newRequestQueue(this)
            queue.add(stringRequest)
        }
    }
}