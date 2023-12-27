package com.example.merona.board

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.merona.R

class RoadSearchActivity : AppCompatActivity() {
    private var browser: WebView? = null

    internal inner class MyJavaScriptInterface {
        @JavascriptInterface
        fun processDATA(data: String?) {
            val extra = Bundle()
//            val intent = Intent(applicationContext,WritingActivity::class.java)
            val broadcaster = LocalBroadcastManager.getInstance(applicationContext)
            val intent = Intent("address")
            intent.putExtra("data", data)
            broadcaster.sendBroadcast(intent)
            Log.d("데이터다~",data.toString())
//            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        browser = findViewById<View>(R.id.address_webView) as WebView
        browser!!.settings.javaScriptEnabled = true
        browser!!.settings.domStorageEnabled = true
        browser!!.addJavascriptInterface(MyJavaScriptInterface(), "Android")
        browser!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                browser!!.loadUrl("javascript:sample2_execDaumPostcode();")
            }
        }
//        browser!!.loadUrl("http://3.36.142.103:8080/map.html")
//        browser!!.loadUrl("http://192.168.219.104:8080/map.html")
        browser!!.loadUrl("http://10.0.2.2:8080/map.html")
    }
}