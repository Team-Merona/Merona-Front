package com.example.merona

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.merona.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btn_addBoard
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_user.*

private const val TAG_HOME = "home_fragment"
private const val TAG_LIST = "list_fragment"
private const val TAG_MESSAGE = "message_fragment"
private const val TAG_USER = "user_fragment"

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding : ActivityMainBinding

    val permission_request = 99
    private lateinit var naverMap: NaverMap
    var permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //btn_addBoard 버튼 클릭 시 게시글 추가 화면 라우팅
        btn_addBoard.setOnClickListener{
            val intent = Intent(this, WritingActivity::class.java)
            startActivity(intent)
        }

        //btn_modify 버튼 클릭 시 프로필 수정 화면 라우팅
//        btn_modify.setOnClickListener {
//            val intent = Intent(this, ModifyActivity::class.java)
//            startActivity(intent)
//        }
//        btn_modify.visibility = View.INVISIBLE


        //bottom bar 설정
        setFragment(TAG_HOME, HomeFragment())

        binding.bottomNavigationview.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.home -> setFragment(TAG_HOME, HomeFragment())
                R.id.list -> setFragment(TAG_LIST, ListFragment())
                R.id.message -> setFragment(TAG_MESSAGE, MessageFragment())
                R.id.user -> setFragment(TAG_USER, UserFragment())
            }
            true
        }
    }

    private fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if(manager.findFragmentByTag(tag) == null) {
            fragTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }

        val home = manager.findFragmentByTag(TAG_HOME)
        val list = manager.findFragmentByTag(TAG_LIST)
        val message = manager.findFragmentByTag(TAG_MESSAGE)
        val user = manager.findFragmentByTag(TAG_USER)

        if(home != null) {
            btn_addBoard.visibility = View.INVISIBLE
            fragTransaction.hide(home)
        }

        if(list != null) {
            fragTransaction.hide(list)
        }

        if(message != null) {
            fragTransaction.hide(message)
        }

        if(user != null) {
//            btn_modify.visibility = View.INVISIBLE
            fragTransaction.hide(user)
        }

        if(tag == TAG_HOME) {
            if(home != null) {
                if(isPermitted()) {
//                    btn_modify.visibility = View.INVISIBLE
                    btn_addBoard.visibility = View.VISIBLE
                    fragTransaction.show(home)
                } else {
                    ActivityCompat.requestPermissions(this, permissions, permission_request)
                }

            }
        }
        else if(tag == TAG_LIST) {
            if(list != null) {
                btn_addBoard.visibility = View.INVISIBLE
//                btn_modify.visibility = View.INVISIBLE
                fragTransaction.show(list)
                val broadcaster = LocalBroadcastManager.getInstance(this)
                val intent = Intent("Board")
                broadcaster.sendBroadcast(intent)
            }
        }
        else if(tag == TAG_MESSAGE) {
            if(message != null) {
                btn_addBoard.visibility = View.INVISIBLE
//                btn_modify.visibility = View.INVISIBLE
                fragTransaction.show(message)
            }
        }
        else if(tag == TAG_USER) {
//            btn_modify.visibility = View.VISIBLE
            if(user != null) {
                btn_addBoard.visibility = View.INVISIBLE
                fragTransaction.show(user)
                val broadcaster = LocalBroadcastManager.getInstance(this)
                val intent = Intent("userBoard")
                broadcaster.sendBroadcast(intent)
            }
        }

        fragTransaction.commitAllowingStateLoss()
    }

    fun isPermitted():Boolean {
        for(perm in permissions) {
            if(ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun startProcess() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map_view) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map_view, it).commit()
            } //권한
        mapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(navaerMap: NaverMap) {
        val cameraPosition = CameraPosition(
            LatLng(37.5666102, 126.9783881),
            16.0
        )
        navaerMap.cameraPosition = cameraPosition
        this.naverMap = naverMap

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setUpdateLocationLister()
    }
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    fun setUpdateLocationLister() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for((i, location) in locationResult.locations.withIndex()) {
                    Log.d("location: ", "${location.latitude}, ${location.longitude}")
                    setLastLocation(location)
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    fun setLastLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)
        val marker = Marker()
        marker.position = myLocation
        marker.map = naverMap

        val cameraUpdate = CameraUpdate.scrollTo(myLocation)
        naverMap.moveCamera(cameraUpdate)
        naverMap.maxZoom = 18.0
        naverMap.minZoom = 5.0
    }
}