package com.example.yeongkkuel.presentation.my.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.my.notification.data.NotificationItem
import com.example.yeongkkuel.presentation.my.notification.data.NotificationType

class NotificationAdapter(
    private val items: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    // 실제로는 섹션을 grouping 하여 items를 구성하거나,
    // items 안에 섹션 아이템을 추가하여 뷰타입을 구별할 수도 있음
    // 여기서는 간단히 item.section != null 이면 헤더라고 가정

    override fun getItemViewType(position: Int): Int {
        // 이전 아이템과 section이 다르면 헤더로 표시하는 식으로 로직 처리
        if (position == 0 ||
            (position > 0 && items[position].section != items[position-1].section)) {
            return VIEW_TYPE_HEADER
        }
        return VIEW_TYPE_ITEM
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
        val item = items[position]
        when (holder) {
            is HeaderViewHolder -> {
                // '내가 리스트의 첫 헤더인지
                val isFirstHeader = (position == 0)
                holder.bind(item, isFirstHeader)
            }
            is NotificationViewHolder -> {
                holder.bind(item, onItemClick)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader = itemView.findViewById<TextView>(R.id.tvHeader)
        private val viewHeaderDivider = itemView.findViewById<View>(R.id.viewHeaderDivider)

        fun bind(item: NotificationItem, isFirstHeader: Boolean) {
            tvHeader.text = item.section

            // 첫 헤더에는 구분선을 숨기고, 두 번째 이후 헤더에는 보여주기
            viewHeaderDivider.visibility = if (isFirstHeader) View.GONE else View.VISIBLE
        }
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon = itemView.findViewById<ImageView>(R.id.ivNotificationIcon)
        private val tvMessage = itemView.findViewById<TextView>(R.id.tvNotificationMessage)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        fun bind(item: NotificationItem, onClick: (NotificationItem) -> Unit) {
            // 아이콘은 NotificationType에 따라 다른 drawable을 설정
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