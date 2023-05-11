package com.example.merona

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.databinding.ActivityMainBinding
import com.example.merona.databinding.ActivityModifyBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_modify.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.check_dialog.*
import kotlinx.android.synthetic.main.fragment_user.*
import org.json.JSONArray
import org.json.JSONObject

class ModifyActivity : AppCompatActivity() {
    private val getDataUrl = "http://10.0.2.2:8080/user/info/"
    private val idUrl = "http://10.0.2.2:8080/user/find/"
    private val modifyUrl = "http://10.0.2.2:8080/user/modify/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify)

        val email = MyApplication.prefs.getString("email","")

        ModifyEmail.isEnabled = true
        ConfirmId2.isEnabled = true

        // 데이터 받아오기
        getData(email)

        backBtn.setOnClickListener{
            onBackPressed()
        }

        ConfirmId2.setOnClickListener{
            // 아이디 보내서 이미 있는 아이디인지 확인
            val stringRequest: StringRequest = object : StringRequest(
                Method.GET, idUrl+ModifyEmail.text.toString(),
                Response.Listener {
                    var strResp = it.toString()
                    if (strResp=="false"){
                        Log.d("중복 아님", ModifyEmail.text.toString())
                        val dlgPopup = ConfirmDialog(this,"사용 가능한 아이디입니다.")
                        dlgPopup.show()
                        dlgPopup.window!!.setLayout(800,450)
                        dlgPopup.setCancelable(false)
                        dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
                        ConfirmId2.isEnabled = false
                        ConfirmId2.setBackgroundDrawable(getDrawable(R.drawable.rectangle_check_button))
                        ModifyEmail.isEnabled = false
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
                    json.put("email", ""+email)
                    return json.toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json; charset=utf-8"
                }
            }
            val queue = Volley.newRequestQueue(this)
            queue.add(stringRequest)
        }

        //수정완료 버튼 클릭 시 ModifyActivity -> userFragment
        ModifyButton.setOnClickListener {
            //데이터를 저장하는 코드
            if(ConfirmId2.isEnabled){
                val dlgPopup = ConfirmDialog(this,"아이디 중복확인을 해주세요.")
                dlgPopup.show()
                dlgPopup.window!!.setLayout(800,450)
                dlgPopup.setCancelable(false)
                dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
            }
            else if(ModifyPw.text.toString()!=ModifyConfirmPassword.text.toString()){
                val dlgPopup = ConfirmDialog(this,"비밀번호가 일치하지 않습니다.")
                dlgPopup.show()
                dlgPopup.window!!.setLayout(800,450)
                dlgPopup.setCancelable(false)
                dlgPopup.okBtn.setOnClickListener{ dlgPopup.cancel() }
            }
            else{
                // 수정된 회원 정보 보내기
                val stringRequest: StringRequest = object : StringRequest(
                    Method.PATCH, modifyUrl+email,
                    Response.Listener {
                        Log.d("수정 완료", ModifyEmail.text.toString())
                        MyApplication.prefs.setString("email",ModifyEmail.text.toString())
                        onBackPressed()
                        val broadcaster = LocalBroadcastManager.getInstance(this)
                        val intent = Intent("profile")
                        broadcaster.sendBroadcast(intent)
                                      },
                    Response.ErrorListener {
                        Log.d("수정 실패", it.toString()) }) {

                    override fun getBody(): ByteArray {
                        val json = JSONObject()
                        json.put("email", ""+ModifyEmail.text.toString())
                        json.put("name", ""+ModifyName.text.toString())
                        json.put("password", ""+ModifyPw.text.toString())
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

    private fun getData(email:String){
        val request=object: StringRequest(
            Request.Method.GET,
            getDataUrl+email,
            Response.Listener<String>{ response ->
                Log.d("응답!",response)
                var strResp = response.toString()
                val jsonObj: JSONObject = JSONObject(strResp)
                val email = jsonObj.getString("email")
                val name = jsonObj.getString("name")
                ModifyEmail.text.append(email)
                ModifyName.text.append(name)
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