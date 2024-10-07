package com.example.merona.board

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.SeekBar
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.R
import com.example.merona.databinding.ActivityBoardModifyBinding
import com.example.merona.util.MyApplication
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class BoardModifyActivity : AppCompatActivity() {
    private var mBinding: ActivityBoardModifyBinding? = null
    private val binding get() = mBinding!!
    private var boardId: Long? = null
    val boardDetailUrl = "http://10.0.2.2:8080/board/list/"
    val boardModifyUrl = "http://10.0.2.2:8080/board/list/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityBoardModifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardId = intent.getLongExtra("boardId", 0)
        setupClickListener()
        setupSeekBar()
        setupModifyBtn()
        setupBoardDetailListener()

    }

    private fun setupClickListener() {
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.modifyButton.setOnClickListener {
            modifyBoard()
        }
    }

    private fun setupBoardDetailListener() {
        val request = object : StringRequest(
            Method.GET,
            boardDetailUrl+boardId.toString(),
            Response.Listener { response ->
                var strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val title = jsonObj.getString("title")
                val contents = jsonObj.getString("contents")
                val addressJsonObject = jsonObj.getJSONObject("address")
                val address = addressJsonObject.getString("streetAddress")
                val detail = addressJsonObject.getString("detailAddress")
                val cost = jsonObj.getString("cost")

                binding.inputTitle.setText(title)
                binding.inputAddress.text = address
                binding.inputDetailAddress.setText(detail)
                binding.inputContents.setText(contents)
                binding.tvPrice.text = cost
                binding.seekBar.progress = (cost.toInt()) / 100
            },
            {
                Log.d("에러!","x..")
            }
        ) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                return try {
                    val utf8String = String(response.data, Charsets.UTF_8)
                    Response.success(utf8String, HttpHeaderParser.parseCacheHeaders(response))
                } catch(e : UnsupportedEncodingException) {
                    Response.error(ParseError(e))
                } catch(e : java.lang.Exception) {
                    Response.error(ParseError(e))
                }
            }

            override fun getParams(): MutableMap<String, String>? {
                val params = HashMap<String, String>()
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headerMap: MutableMap<String, String> = HashMap()
                headerMap["Content-Type"] = "application/json"
                headerMap["Authorization"] = "Bearer "+ MyApplication.prefs.getString("accessToken","")
                return headerMap
            }
        }

        val queue = Volley.newRequestQueue(this)
        queue.add(request)
    }

    private fun setupSeekBar() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvPrice.text = (progress*100).toString()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupModifyBtn() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val title = binding.inputTitle.text.toString()
                val address = binding.inputAddress.text.toString()
                val detailAddress = binding.inputDetailAddress.text.toString()

                binding.modifyButton.isEnabled = title.isNotEmpty() && address.isNotEmpty() && detailAddress.isNotEmpty()
                if(binding.modifyButton.isEnabled) {
                    binding.modifyButton.setBackgroundResource(R.drawable.button_round_79d4682)
                } else {
                    binding.modifyButton.setBackgroundResource(R.drawable.button_round_79d468)
                }
            }
        }
        binding.inputTitle.addTextChangedListener(textWatcher)
        binding.inputAddress.addTextChangedListener(textWatcher)
        binding.inputDetailAddress.addTextChangedListener(textWatcher)
    }

    private fun modifyBoard() {
        val request = object : StringRequest(
            Method.PATCH,
            boardModifyUrl+boardId.toString()+"/update",
            Response.Listener {
                val intent = Intent(this, BoardDetailActivity::class.java)
                intent.putExtra("id", boardId)
                startActivity(intent)
                finish()
            },
            Response.ErrorListener { Log.d("error", "update fail") }
        ) {
            override fun parseNetworkResponse(response: NetworkResponse): Response<String>? {
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
                json.put("cost", binding.tvPrice.text.toString())
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
        queue.add(request)
    }
}