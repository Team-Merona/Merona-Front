package com.example.merona

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import kotlinx.android.synthetic.main.check_dialog.*

class ConfirmDialog(context: Context, text: String) : Dialog(context) {
    private val dialog = Dialog(context)
    private var text: String? = null

    init {
        this.text = text
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.check_dialog)

        // 배경을 투명하게함
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        contentText.setText(text)

        window!!.attributes.y = 500
        window!!.attributes.gravity = Gravity.TOP + Gravity.CENTER_HORIZONTAL
    }
}