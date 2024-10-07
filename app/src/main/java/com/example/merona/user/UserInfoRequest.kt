package com.example.merona.user

data class UserInfoRequest(
    var email: String? = null,
    var password: String? = null,
    var name : String? = null,
    var phoneNumber : String? = null
) {
    fun isEmptyValue(): Boolean {
        if (email!!.isEmpty() || password!!.isEmpty() || name!!.isEmpty() || phoneNumber!!.isEmpty()) {
            return true
        }
        return false
    }

    fun checkPassword(checkPassword: String?): Boolean {
        if (password != checkPassword) {
            return false
        }
        return true
    }
}