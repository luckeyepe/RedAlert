package com.example.mickey.redalert.view_holders

import android.content.Context
import android.content.Intent
import com.example.mickey.redalert.R
import com.example.mickey.redalert.activities.chat_activities.MaxImageActivity
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.row_message_from_image_item.view.*

class ChatLogRecieveImageItemViewHolder(val userPhotoURL: String, val text: String, val context: Context): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.row_message_from_image_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val profilePicture = viewHolder.itemView.circleImageView_messageFromImageItemRowProfilePic
        val message = viewHolder.itemView.imageView_messageFromImageItemRowImage

        if (userPhotoURL != "default") {
            Picasso.get().load(userPhotoURL).into(profilePicture)
        }else{
            Picasso.get().load(R.drawable.default_avata).into(profilePicture)
        }

        if (text.isNullOrEmpty()){
            Picasso.get().load(R.drawable.ic_error_black_24dp).into(message)
        }else {
            Picasso.get().load(text).into(message)
        }

        message.setOnClickListener {
            var intent = Intent(context, MaxImageActivity::class.java)
            intent.putExtra("sourceUrl", text)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}