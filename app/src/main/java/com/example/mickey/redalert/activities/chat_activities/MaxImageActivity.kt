package com.example.mickey.redalert.activities.chat_activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mickey.redalert.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_max_image.*

class MaxImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_max_image)

        val sourcUrl = intent.getStringExtra("sourceUrl")

        Picasso.get().load(sourcUrl).into(imageView_maxImageImage)
    }
}
