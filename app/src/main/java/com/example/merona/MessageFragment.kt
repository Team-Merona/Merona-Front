package com.example.merona

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import kotlinx.android.synthetic.main.fragment_message.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class MessageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        //채팅하기 버튼 클릭 시 채팅하기 화면으로
        val chatBtn : AppCompatButton = view.findViewById(R.id.chat_btn)
        chatBtn.setOnClickListener {
            val intent = Intent(getActivity(), ChatActivity::class.java)
            //destinationUid에 게시글 작성자의 uid를 전달.
            intent.putExtra("destinationUid", "a")
            startActivity(intent)
        }

        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}