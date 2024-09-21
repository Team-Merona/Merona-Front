package com.example.merona.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.R
import com.example.merona.databinding.ActivityUserModifyBinding
import com.example.merona.dialog.ConfirmDialog
import com.example.merona.util.MyApplication
import com.google.gson.Gson
import org.json.JSONObject

class UserModifyActivity : AppCompatActivity() {
    private val getDataUrl = "/user/info/"
    private val idCheckUrl = "/user/find/"
    private val modifyUrl = "/user/modify/"

    private var mBinding: ActivityUserModifyBinding? = null
    private val binding get() = mBinding!!

    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityUserModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setClickListener()
        setScreen()

        email = MyApplication.prefs.getString("email", "")
        getData(email!!)
    }

    private fun getData(email: String) {
        val request = object : StringRequest(Method.GET,
            getString(R.string.prefix_uri) + getDataUrl + email,
            Response.Listener<String> { response ->
                Log.d("응답!", response)
                var strResp = response.toString()
                val jsonObj: JSONObject = JSONObject(strResp)
                val email = jsonObj.getString("email")
                val name = jsonObj.getString("name")
                binding.email.text.append(email)
                binding.name.text.append(name)
            },
            {
                Log.d("에러!", it.message!!)
                onBackPressed()
            }

        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["Content-Type"] = "application/json"
                headerMap["Authorization"] =
                    "Bearer " + MyApplication.prefs.getString("accessToken", "")
                return headerMap
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    private fun setClickListener() {
        // 중복 확인 버튼
        binding.idCheckBtn.setOnClickListener {
            // 아이디 보내서 이미 있는 아이디인지 확인
            val stringRequest: StringRequest = object : StringRequest(Method.GET,
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

        // 수정하기 버튼
        binding.modifyBtn.setOnClickListener {
            val modifyRequest = UserModifyRequest(
                binding.email.text.toString(),
                binding.password.text.toString(),
                binding.name.text.toString()
            )

            if (modifyRequest.isEmptyValue()) {
                ConfirmDialog(this, getString(R.string.error_register_empty)).createDialog()
            } else if ((binding.email.text.toString() != email) && binding.idCheckBtn.isEnabled) {
                // 기존 아이디와 입력 아이디가 다른데 중복 검사를 하지 않은 경우
                ConfirmDialog(this, getString(R.string.error_user_eamil)).createDialog()
            } else if (!modifyRequest.checkPassword(binding.passwordCheck.text.toString())) {
                ConfirmDialog(this, getString(R.string.error_user_password)).createDialog()
            } else {
                // 수정된 회원 정보 보내기
                val stringRequest: StringRequest = object : StringRequest(Method.PATCH,
                    getString(R.string.prefix_uri) + modifyUrl + email,
                    Response.Listener {
                        Log.d("수정 완료", modifyRequest.email!!)
                        MyApplication.prefs.setString("email", modifyRequest.email!!)
                        onBackPressed()
                        val broadcaster = LocalBroadcastManager.getInstance(this)
                        val intent = Intent("profile")
                        broadcaster.sendBroadcast(intent)
                    },
                    Response.ErrorListener {
                        Log.d("수정 실패", it.toString())
                        ConfirmDialog(this, getString(R.string.error_user_modify)).createDialog()
                    }) {

                    override fun getBody(): ByteArray {
                        val json = Gson().toJson(modifyRequest)
                        return json.toString().toByteArray()
                    }

                    override fun getBodyContentType(): String? {
                        return "application/json; charset=utf-8"
                    }

                    override fun getHeaders(): MutableMap<String, String> {
                        val headerMap: MutableMap<String, String> = HashMap()
                        headerMap["Content-Type"] = "application/json"
                        headerMap["Authorization"] =
                            "Bearer " + MyApplication.prefs.getString("accessToken", "")
                        return headerMap
                    }
                }

                val queue = Volley.newRequestQueue(this)
                queue.add(stringRequest)
            }
        }
    }

    private fun setScreen() {
        binding.idCheckBtn.isEnabled = true
    }
}