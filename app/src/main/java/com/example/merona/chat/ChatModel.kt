package com.example.merona.chat

//채팅 메세지를 담을 모델
//class ChatModel (val users: HashMap<String, Boolean> = HashMap(),
//                 val comments : HashMap<String, Comment> = HashMap()) {
//    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
//}

class ChatModel (val boardId : HashMap<String, Boolean> = HashMap(),
                 val users: HashMap<String, Boolean> = HashMap(),
                 val comments : HashMap<String, Comment> = HashMap()) {
    class Comment(val uid: String? = null, val message: String? = null, val time: String? = null)
}