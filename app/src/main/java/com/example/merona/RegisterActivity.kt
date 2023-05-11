package com.example.merona

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.check_dialog.*
import org.json.JSONArray
import org.json.JSONObject


class RegisterActivity : AppCompatActivity() {
    val registerUrl = "http://10.0.2.2:8080/user/signup"
    val idUrl = "http://10.0.2.2:8080/user/find/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        ConfirmId.isEnabled = true
        addEmail.isEnabled = true

        // 중복 확인
        ConfirmId.setOnClickListener{
            // 아이디 보내서 이미 있는 아이디인지 확인
            val stringRequest: StringRequest = object : StringRequest(
                Method.GET, idUrl+addEmail.text.toString(),
                Response.Listener {
                    var strResp = it.toString()
                    if (strResp=="false"){
                        Log.d("중복 아님", addEmail.text.toString())
                        val dlgPopup = ConfirmDialog(this,"사용 가능한 아이디입니다.")
                        dlgPopup.show()
                        dlgPopup.window!!.setLayout(800,450)
                        dlgPopup.setCancelable(false)
                        dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
                        ConfirmId.isEnabled = false
                        ConfirmId.setBackgroundDrawable(getDrawable(R.drawable.rectangle_check_button))
                        addEmail.isEnabled = false
                    }
                    else{
                        Log.d("중복임", it.toString())
                        val dlgPopup = ConfirmDialog(this,"중복입니다. 다시 입력해주세요.")
                        dlgPopup.show()
                        dlgPopup.window!!.setLayout(800,450)
                        dlgPopup.setCancelable(false)
                        dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
                    }
                    },
                Response.ErrorListener {
                    Log.d("error",it.toString())
                }) {

                override fun getBody(): ByteArray {
                    val json = JSONObject()
                    json.put("email", ""+addEmail.text.toString())
                    return json.toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json; charset=utf-8"
                }
            }
            val queue = Volley.newRequestQueue(this)
            queue.add(stringRequest)
        }

        //가입하기 버튼 클릭 시 화면 전환
        RegisterButton.setOnClickListener {
            if(ConfirmId.isEnabled){
                val dlgPopup = ConfirmDialog(this,"아이디 중복확인을 해주세요.")
                dlgPopup.show()
                dlgPopup.window!!.setLayout(800,450)
                dlgPopup.setCancelable(false)
                dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
            }
            else if(addPwd.text.toString()!=ConfirmPassword.text.toString()){
                val dlgPopup = ConfirmDialog(this,"비밀번호가 일치하지 않습니다.")
                dlgPopup.show()
                dlgPopup.window!!.setLayout(800,450)
                dlgPopup.setCancelable(false)
                dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
            }
            else{
                // 회원 정보 보내기
                val stringRequest: StringRequest = object : StringRequest(
                    Method.POST, registerUrl,
                    Response.Listener {
                        Log.d("회원가입 완료", addEmail.text.toString())
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)},
                    Response.ErrorListener {
                        Log.d("회원가입 실패", it.toString()) }) {

                    override fun getBody(): ByteArray {
                        val json = JSONObject()
                        json.put("email", ""+addEmail.text.toString())
                        json.put("name", ""+addName.text.toString())
                        json.put("password", ""+addPwd.text.toString())
                        json.put("phoneNumber", ""+Input_Phone.text.toString())
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

        //뒤로가기 버튼 만들기
        val toolbar = findViewById<Toolbar>(R.id.back_login)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.left_arrow)

    }

}