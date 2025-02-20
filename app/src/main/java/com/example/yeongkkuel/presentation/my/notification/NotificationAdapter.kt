package com.example.yeongkkuel.presentation.my.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.my.notification.data.NotificationItem
import com.example.yeongkkuel.presentation.my.notification.data.NotificationListItem
import com.example.yeongkkuel.presentation.my.notification.data.NotificationType

class NotificationAdapter(
    private val displayItems: List<NotificationListItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is NotificationListItem.HeaderItem -> VIEW_TYPE_HEADER
            is NotificationListItem.NormalItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_notification, parent, false)
                NotificationViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is NotificationListItem.HeaderItem -> {
                (holder as HeaderViewHolder).bind(item)
            }
            is NotificationListItem.NormalItem -> {
                (holder as NotificationViewHolder).bind(item.notification, onItemClick)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private val viewHeaderDivider: View? = itemView.findViewById(R.id.viewHeaderDivider)

        fun bind(headerItem: NotificationListItem.HeaderItem) {
            tvHeader.text = headerItem.sectionName

            // "첫 헤더만 구분선을 안 보이게" 등등의 조건을 주고 싶으면,
            // adapterPosition == 0 인지 검사할 수도 있습니다.
            viewHeaderDivider?.visibility = if (adapterPosition == 0) View.GONE else View.VISIBLE
        }
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivNotificationIcon)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvNotificationMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(item: NotificationItem, onClick: (NotificationItem) -> Unit) {
            // 아이콘은 NotificationType에 따라 결정
            val iconRes = when (item.type) {
                NotificationType.CHALLENGE_JOIN -> R.drawable.ic_challenge_join
                NotificationType.RANKING_REWARD -> R.drawable.ic_ranking_reward
                NotificationType.NO_SPEND_REWARD -> R.drawable.ic_no_spend_reward
                NotificationType.DAILY_EXCEED -> R.drawable.ic_daily_exceed
                NotificationType.CHALLENGE_RANKING_UPDATE -> R.drawable.ic_ranking_update
            }
            ivIcon.setImageResource(iconRes)

            tvMessage.text = item.message
            tvTime.text = item.timeText

            itemView.setOnClickListener {
                onClick(item)
            }
        }
    }
}