package com.example.merona.board

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import android.widget.SeekBar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.R
import com.example.merona.util.MyApplication
import com.example.merona.databinding.ActivityWritingBinding
import kotlinx.android.synthetic.main.activity_writing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class BoardWritingActivity : AppCompatActivity() {
    val writingUrl = "http://10.0.2.2:8080/board/save"

    private var mBinding : ActivityWritingBinding? = null
    private val binding get() = mBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityWritingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // broadcast 등록
        register()
        setupPostBtn()
        setupClickListener()
        setupSeekBar()
    }

    private fun register() {
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(
            addressReceiver , IntentFilter("address")
        )
    }

    private fun unRegister() {
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(addressReceiver)
    }

    private fun setupPostBtn() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val title = binding.inputTitle.text.toString()
                val address = binding.inputAddress.text.toString()
                val detailAddress = binding.inputDetailAddress.text.toString()

                binding.PostButton.isEnabled = title.isNotEmpty() && address.isNotEmpty() && detailAddress.isNotEmpty()
                if(binding.PostButton.isEnabled) {
                    binding.PostButton.setBackgroundResource(R.drawable.button_round_79d4682)
                } else {
                    binding.PostButton.setBackgroundResource(R.drawable.button_round_79d468)
                }
            }
        }

        binding.inputTitle.addTextChangedListener(textWatcher)
        binding.inputAddress.addTextChangedListener(textWatcher)
        binding.inputDetailAddress.addTextChangedListener(textWatcher)
    }
    private fun setupClickListener() {

        binding.backBtn.setOnClickListener{ onBackPressed() }

        binding.inputAddress.setOnClickListener{
            val intent = Intent(this, RoadSearchActivity::class.java)
            startActivity(intent)
        }

        binding.PostButton.setOnClickListener {
            postBoard()
        }
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                tv_price.text = (p1*100).toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun postBoard() {
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, writingUrl,
            Response.Listener {
                Log.d("successful Post", inputTitle.text.toString())
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
                    Response.error(ParseError(e))
                } catch (e: Exception) {
                    Response.error(ParseError(e))
                }
            }

            override fun getBody(): ByteArray {
                val json = JSONObject()
                json.put("title", binding.inputTitle.text.toString())
                json.put("contents", binding.inputContents.text.toString())
                json.put("streetAddress", binding.inputAddress.text.toString())
                json.put("detailAddress", binding.inputDetailAddress.text.toString())
                json.put("cost", binding.tvPrice.text.toString().toInt())
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

    private val addressReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d("addressReceiver", "Intent: $intent")
            binding.inputAddress.text = intent.getStringExtra("data").toString()
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
                    binding.inputAddress.setText("($zoneCode) $roadAddress $buildingName")
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