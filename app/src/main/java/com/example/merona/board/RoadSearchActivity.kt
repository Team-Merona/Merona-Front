package com.example.merona.board

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.merona.databinding.ActivityWebViewBinding

class RoadSearchActivity : AppCompatActivity() {
    private var browser: WebView? = null
    private var mBinding : ActivityWebViewBinding? = null
    private val binding get() = mBinding!!

    internal inner class MyJavaScriptInterface {
        @JavascriptInterface
        fun processDATA(data: String?) {
            val extra = Bundle()
            val broadcaster = LocalBroadcastManager.getInstance(applicationContext)
            val intent = Intent("address")
            intent.putExtra("data", data)
            broadcaster.sendBroadcast(intent)
            Log.d("Address",data.toString())
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        browser = binding.addressWebView
        browser!!.settings.javaScriptEnabled = true
        browser!!.settings.domStorageEnabled = true
        browser!!.addJavascriptInterface(MyJavaScriptInterface(), "Android")
        browser!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                browser!!.loadUrl("javascript:sample2_execDaumPostcode();")
            }
        }
        browser!!.loadUrl("http://10.0.2.2:8080/map.html")
    }
}