package com.example.merona.user

data class UserModifyRequest(
    var email: String? = null,
    var password: String? = null,
    var name: String? = null
) {
    fun isEmptyValue(): Boolean {
        if (email!!.isEmpty() || password!!.isEmpty() || name!!.isEmpty()) {
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