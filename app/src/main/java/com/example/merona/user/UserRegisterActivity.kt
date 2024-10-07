package com.example.merona.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.R
import com.example.merona.databinding.ActivityRegisterBinding
import com.example.merona.dialog.ConfirmDialog
import com.google.gson.Gson

class UserRegisterActivity : AppCompatActivity() {
    val registerUrl = "/user/signup"
    val idCheckUrl = "/user/find/"

    private var mBinding: ActivityRegisterBinding? = null
    private val binding get() = mBinding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListener()
        setScreen()
    }

    private fun setClickListener() {
        // 중복 확인
        binding.idCheckBtn.setOnClickListener {
            // 아이디 보내서 이미 있는 아이디인지 확인
            val stringRequest: StringRequest = object : StringRequest(
                Method.GET,
                getString(R.string.prefix_uri) + idCheckUrl + binding.email.text.toString(),
                Response.Listener {
                    var strResp = it.toString()
                    if (strResp == "false") {
                        Log.d("중복 아님", binding.email.text.toString())
                        ConfirmDialog(this, getString(R.string.success_check_email)).createDialog()
                        binding.idCheckBtn.isEnabled = false
                        binding.idCheckBtn.setBackgroundDrawable(getDrawable(R.drawable.button_round_check_334c4c4c))
                        binding.email.isEnabled = false
                    } else {
                        Log.d("중복임", it.toString())
                        ConfirmDialog(this, getString(R.string.error_check_email)).createDialog()
                    }
                },
                Response.ErrorListener {
                    Log.d("error", it.toString())
                }) {

                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }
            }
            val queue = Volley.newRequestQueue(this)
            queue.add(stringRequest)
        }

        //가입하기 버튼
        binding.registerBtn.setOnClickListener {
            val registerRequest = UserInfoRequest(
                binding.email.text.toString(),
                binding.password.text.toString(),
                binding.name.text.toString(),
                binding.phone.text.toString()
            )

            if (registerRequest.isEmptyValue()) {
                ConfirmDialog(this, getString(R.string.error_register_empty)).createDialog()
            } else if (binding.idCheckBtn.isEnabled) {
                ConfirmDialog(this, getString(R.string.error_user_eamil)).createDialog()
            } else if (!registerRequest.checkPassword(binding.passwordCheck.text.toString())) {
                ConfirmDialog(this, getString(R.string.error_user_password)).createDialog()
            } else {
                // 회원 정보 보내기
                val stringRequest: StringRequest = object : StringRequest(
                    Method.POST,
                    getString(R.string.prefix_uri) + registerUrl,
                    Response.Listener {
                        Log.d("회원가입 완료", registerRequest.email!!)
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    },
                    Response.ErrorListener {
                        Log.d("회원가입 실패", it.toString())
                        ConfirmDialog(this, getString(R.string.error_register)).createDialog()
                    }) {

                    override fun getBody(): ByteArray {
                        val json = Gson().toJson(registerRequest)
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

    private fun setScreen() {
        binding.idCheckBtn.isEnabled = true

        //뒤로가기 버튼
        val toolbar = findViewById<Toolbar>(R.id.back_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.img_back)
    }
}