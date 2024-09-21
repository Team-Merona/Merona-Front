package com.example.merona.board

enum class BoardState(private val kor: String) {
    REQUEST_WAITING( "대기중"),
    REQUEST_ON_GOING("진행중"),
    REQUEST_COMPLETE("완료");

    fun getKorState() : String {
        return kor
    }
}
