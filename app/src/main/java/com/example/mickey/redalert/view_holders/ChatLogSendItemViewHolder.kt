package com.example.mickey.redalert.view_holders

import com.example.mickey.redalert.R
import com.example.mickey.redalert.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.row_message_to_item.view.*

class ChatLogSendItemViewHolder(val userPhotoURL: String, val text: String) : Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.row_message_to_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val profilePicture = viewHolder.itemView.circleImageView_messageToItemRowProfilePic
        val message = viewHolder.itemView.textView_messageToItemRowMessage

        if (userPhotoURL != "default") {
            Picasso.get().load(userPhotoURL).into(profilePicture)
        }else{
            Picasso.get().load(R.drawable.default_avata).into(profilePicture)
        }

        message.text = text
    }
}