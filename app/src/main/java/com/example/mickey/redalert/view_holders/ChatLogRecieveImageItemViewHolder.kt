package com.example.mickey.redalert.view_holders

import com.example.mickey.redalert.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.row_message_from_image_item.view.*

class ChatLogRecieveImageItemViewHolder(val userPhotoURL: String, val text: String): Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.row_message_from_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val profilePicture = viewHolder.itemView.circleImageView_messageFromImageItemRowProfilePic
        val message = viewHolder.itemView.imageView_messageFromImageItemRowImage
        Picasso.get().load(userPhotoURL).into(profilePicture)
        Picasso.get().load(text).into(message)
    }
}