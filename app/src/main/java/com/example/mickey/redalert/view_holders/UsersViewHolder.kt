package com.example.mickey.redalert.view_holders

import com.example.mickey.redalert.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.row_user.view.*

class UsersViewHolder(val user: com.example.mickey.redalert.models.User): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val userPhoto = viewHolder.itemView.circleImageView_userRowUserPhoto
        val userFullName = viewHolder.itemView.textView_userRowFullName

        if (user.user_profilePictureURL != "default") {
            Picasso.get().load(user.user_profilePictureURL).into(userPhoto)
        }else{
            Picasso.get().load(R.drawable.default_avata).into(userPhoto)
        }

        userFullName.text = "${user.user_firstName} ${user.user_lastName}"
    }

    override fun getLayout(): Int {
        return R.layout.row_user
    }
}