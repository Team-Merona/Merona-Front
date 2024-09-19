package com.example.merona.user

data class LoginRequest(
    var email: String? = null,
    var password: String? = null
)