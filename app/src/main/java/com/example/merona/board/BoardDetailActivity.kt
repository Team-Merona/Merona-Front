package com.example.merona.board

import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.merona.util.MyApplication
import com.example.merona.R
import com.example.merona.chat.ChatActivity
import com.example.merona.databinding.ActivityDetailBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException

class BoardDetailActivity : AppCompatActivity(), OnMapReadyCallback{
    private var mBinding : ActivityDetailBinding? = null
    private val binding get() = mBinding!!
    val boardDetailUrl = "http://10.0.2.2:8080/board/list/"
    var email : String? = null

    //지오코딩
    private lateinit var naverMap: NaverMap
    var list : List<Address>? = null
    var boardId : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boardId = intent.getLongExtra("id",0)

        setupClickListener()
        setupBoardDetailListener()
    }
    private fun setupClickListener() {
        binding.chatBtn.setOnClickListener {
            if(email == MyApplication.prefs.getString("email", "")) {
                val intent = Intent(this, BoardModifyActivity::class.java)
                intent.putExtra("boardId", boardId)
                startActivity(intent)
            } else {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("destinationUId", email) //작성자ID
                intent.putExtra("boardId", boardId)
                startActivity(intent)
            }
        }
    }

    private fun setupBoardDetailListener() {
        val request=object: StringRequest(
            Method.GET,
            boardDetailUrl+boardId.toString(),
            Response.Listener { response ->
                var strResp = response.toString()
                val jsonObj = JSONObject(strResp)
                val id = jsonObj.getLong("id")
                val title = jsonObj.getString("title")
                val contents = jsonObj.getString("contents")
                val addressJsonObject = jsonObj.getJSONObject("address")
                val address = addressJsonObject.getString("streetAddress")
                val detail = addressJsonObject.getString("detailAddress")
                val cost = jsonObj.getString("cost")
                var state = jsonObj.getString("state")
                email = jsonObj.getString("email")
                binding.tvName.text = email+"님"
                if (email== MyApplication.prefs.getString("email","")){
                    binding.chatBtn.text = "수정하기"
                }
                binding.tvTitle.text = title
                binding.tvContents.text = contents
                binding.tvCost.text = cost.toString()+"원"

                //Geocoder 사용 : 주소 -> 위도, 경도로 변환
                binding.addressText.text = address.toString() + "\n" + detail.toString()
                val geocoder = Geocoder(this)
                val str = address.toString()

                try {
                    list = geocoder.getFromLocationName(str, 10)
                } catch (e: IOException) { }

                if(list != null) {
                    if(list!!.isEmpty()) {
                        binding.addressText.text = "존재하지 않는 주소입니다."
                    }
                    else {
                        val fm = supportFragmentManager
                        val mapFragment = fm.findFragmentById(R.id.detail_map) as MapFragment
                        mapFragment.getMapAsync(this)
                    }
                }

                if (state=="REQUEST_WAITING"){
                    state = "요청 대기중"
                }
                else if(state =="REQUEST_ON_GOING"){
                    state = "요청 진행중"
                }
                else{
                    state="요청 완료"
                    binding.requestBtn.setBackgroundResource(R.drawable.button_round_gray)
                    binding.requestBtn.isEnabled = false
                    binding.requestBtn.setTextColor(Color.DKGRAY)
                }
                binding.requestBtn.text = state

            },
            {
                Log.d("에러!","x..")
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

            override fun getParams():MutableMap<String,String>{
                val params=HashMap<String,String>()
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

    //장소로 이동 + 마커 표시
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        val latitude = list!!.get(0).latitude
        val longitude = list!!.get(0).longitude
        val cameraPosition = CameraPosition(
            LatLng(latitude, longitude), //위치 지정
            16.0 //줌 레벨
        )
        naverMap.cameraPosition = cameraPosition
        val Location = LatLng(latitude, longitude)
        val marker = Marker()
        marker.position = Location
        marker.map = naverMap
        //마커
        val cameraUpdate = CameraUpdate.scrollTo(Location)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0
    }
}