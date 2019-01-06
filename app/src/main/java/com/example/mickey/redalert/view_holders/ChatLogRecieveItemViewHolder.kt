package com.example.mickey.redalert.view_holders

import com.example.mickey.redalert.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatLogRecieveItemViewHolder: Item<ViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.row_message_from_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

    }
}