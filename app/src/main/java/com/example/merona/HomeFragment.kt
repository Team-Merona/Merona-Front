package com.example.merona

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_detail.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import kotlin.contracts.contract


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var param1: String? = null
    private var param2: String? = null

    private var PERMISSION_REQUEST_CODE = 100

    private lateinit var naverMap: NaverMap
    private lateinit var mLocationSource: FusedLocationSource

    private var boardlistUrl = "http://10.0.2.2:8080/board/list"

    //위도, 경도 저장
    var list : List<Address>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        startProcess()
    }
    fun startProcess() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_view, it).commit()
            } //권한
        //getMapAsync를 호출하여 비동기로 onMapReady 콜백 메소드 호출
        mapFragment.getMapAsync(this)

        //위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource = FusedLocationSource(this, PERMISSION_REQUEST_CODE)
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        val cameraPosition = CameraPosition(
            LatLng(37.5666102, 126.9783881), //위치 지정
            16.0 //줌 레벨
        )
        naverMap.cameraPosition = cameraPosition

        this.naverMap = naverMap
        naverMap.locationSource = mLocationSource

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity()) //gps 자동으로 받아오기
        //현재 내 위치를 계속 tracking하도록 설정
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        //각 게시글의 주소를 위도,경도로 저장(지오코딩)하여 마커 표시
        val itemList = ArrayList<BoardItem>()

        val request=object: StringRequest(
            Request.Method.GET,
            boardlistUrl,
            Response.Listener<String>{ response ->
                Log.d("응답!",response)
                var strResp = response.toString()
                val jsonArray = JSONArray(strResp)
                for (i in 0..jsonArray.length()-1){
                    val jsonObject = jsonArray.getJSONObject(i)

                    val id = jsonObject.getLong("id")
                    val title = jsonObject.getString("title")
                    val addressJsonObject = jsonObject.getJSONObject("address")
                    val address = addressJsonObject.getString("streetAddress")
                    val cost = jsonObject.getInt("cost")

                    itemList.add(BoardItem(id!!,title,address,cost.toString()+"원"))

                }

                Log.d("저장!", itemList.toString())

                Log.d("itemList size", itemList.size.toString())

                for(i in 0 until itemList.size) {
                    val geocoder : Geocoder = Geocoder(requireContext())
                    val str = itemList[i].address
                    try {
                        list = geocoder.getFromLocationName(str, 10)
                    } catch(e: Exception){ }
                    val latitude = list!!.get(0).latitude
                    val longitude = list!!.get(0).longitude
                    val Location = LatLng(latitude, longitude)

                    var marker = Marker()
                    marker.position = Location
                    marker.map = naverMap
                    var boardId = itemList[i].id
                    marker.apply {
                        setOnClickListener {

                            val intent = Intent(requireActivity(), DetailActivity::class.java)
                            intent.putExtra("id", boardId)
                            Log.d("HomeFragment-boardId", id.toString())
                            startActivity(intent)
                            true
                        }
                    }
                }
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
                    // log error
                    Response.error(ParseError(e))
                } catch (e: Exception) {
                    // log error
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
                headerMap["Authorization"] = "Bearer "+MyApplication.prefs.getString("accessToken","")
                return headerMap
            }
        }

        val queue = Volley.newRequestQueue(context)
        queue.add(request)
    }

    //내 위치를 가져오는 코드
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient //자동으로 gps값을 받아온다.

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }
}


