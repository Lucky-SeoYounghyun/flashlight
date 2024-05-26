package com.strongflashlight.flashlight

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.text.HtmlCompat
import android.text.method.LinkMovementMethod
import android.view.View

class DetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""

        val detailTextView: TextView = findViewById(R.id.detailTextView)
        val contentTextView: TextView = findViewById(R.id.contentTextView)
        val backArrow: Button = findViewById(R.id.backArrow)

        detailTextView.text = title

        // Convert to HTML to process <br> tags and other HTML elements
        val htmlProcessedContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY)
        contentTextView.text = htmlProcessedContent
        contentTextView.movementMethod = LinkMovementMethod.getInstance()

        // Set the click listener for the back arrow
        backArrow.setOnClickListener {
            finish()  // 현재 액티비티를 종료하여 이전 화면으로 돌아갑니다.
        }
    }
}