package com.example.merona

//채팅 메세지를 담을 모델
class ChatModel (val users: HashMap<String, Boolean> = HashMap(),
                 val comments : HashMap<String, Comment> = HashMap()) {
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}