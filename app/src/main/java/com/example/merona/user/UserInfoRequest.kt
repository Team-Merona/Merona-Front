package com.example.merona.user

data class UserInfoRequest(
    var email: String? = null,
    var password: String? = null,
    var name : String? = null,
    var phoneNumber : String? = null
)