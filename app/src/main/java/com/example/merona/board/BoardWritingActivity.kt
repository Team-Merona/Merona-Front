package com.example.merona.board

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.util.MyApplication
import com.example.merona.R
import kotlinx.android.synthetic.main.activity_modify.*

import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_writing.*
import kotlinx.android.synthetic.main.dialog_check.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class BoardWritingActivity : AppCompatActivity() {
//    val writingUrl = "http://3.36.142.103:8080/board/save"
    val writingUrl = "http://10.0.2.2:8080/board/save"
//    val writingUrl = "http://172.30.1.5:8080/board/save"
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_writing)

        // broadcast 등록
        register()

        PostButton.isEnabled = true

        //뒤로가기 버튼 만들기
        val toolbar = findViewById<Toolbar>(R.id.back_home)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.img_back)

        inputAddress.text=intent.getStringExtra("data").toString()
        Log.d("Writing Acitivy 데이터~",intent.getStringExtra("data").toString())
        inputAddress.setOnClickListener{
//            showKakaoAddressWebView()
            val intent = Intent(this, RoadSearchActivity::class.java)
            startActivity(intent)
        }


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

                override fun getBody(): ByteArray {
                    val json = JSONObject()
                    json.put("title", ""+inputTitle.text.toString())
                    json.put("contents", ""+inputContents.text.toString())
                    json.put("streetAddress",""+inputAddress.text.toString())
                    json.put("cost", tv_price.text.toString().toInt())
                    return json.toString().toByteArray()
                }

                override fun getBodyContentType(): String? {
                    return "application/json; charset=utf-8"
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headerMap: MutableMap<String, String> = HashMap()
                    headerMap["Content-Type"] = "application/json"
                    headerMap["Authorization"] = "Bearer "+ MyApplication.prefs.getString("accessToken","")
                    return headerMap
                }
            }
            val queue = Volley.newRequestQueue(this)
            queue.add(stringRequest)
        }

        // 시크바
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                tv_price.text = (p1*100).toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

    }

    private fun register() {
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            addressReceiver , IntentFilter("address")
        )
    }

    fun unRegister() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(addressReceiver)
    }

    private val addressReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("addressReceiver", "Intent: $intent")
            val address : Button = findViewById(R.id.inputAddress)
            address.text = intent.getStringExtra("data").toString()
        }
    }

    private fun showKakaoAddressWebView() {

        webView.settings.apply {
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }

        webView.apply {
            addJavascriptInterface(WebViewData(), "MysosoApp")
            webViewClient = client
            webChromeClient = chromeClient
            loadUrl("http://10.0.2.2:8080/map.html")
//            loadUrl("http://192.168.219.106:8080/map.html")
        }

    }

    private val client: WebViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return false
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.proceed()
        }
    }

    private inner class WebViewData {
        @JavascriptInterface
        fun getAddress(zoneCode: String, roadAddress: String, buildingName: String) {

            CoroutineScope(Dispatchers.Default).launch {

                withContext(CoroutineScope(Dispatchers.Main).coroutineContext) {

                    inputAddress.setText("($zoneCode) $roadAddress $buildingName")

                }
            }
        }
    }

    private val chromeClient = object : WebChromeClient() {

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {

            val newWebView = WebView(applicationContext)

            newWebView.settings.javaScriptEnabled = true

            val dialog = Dialog(applicationContext)

            dialog.setContentView(newWebView)

            val params = dialog.window!!.attributes

            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.attributes = params
            dialog.show()

            newWebView.webChromeClient = object : WebChromeClient() {
                override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                    super.onJsAlert(view, url, message, result)
                    webView.loadUrl("javascript:sample2_execDaumPostcode();");
                    return true
                }

                override fun onCloseWindow(window: WebView?) {
                    dialog.dismiss()
                }
            }

            (resultMsg!!.obj as WebView.WebViewTransport).webView = newWebView
            resultMsg.sendToTarget()

            return true
        }
    }
}