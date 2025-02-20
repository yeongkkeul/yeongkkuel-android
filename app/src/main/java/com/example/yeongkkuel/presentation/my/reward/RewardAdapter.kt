package com.example.yeongkkuel.presentation.my.reward

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.R
import com.example.yeongkkuel.presentation.my.notification.NotificationAdapter.HeaderViewHolder
import com.example.yeongkkuel.presentation.my.notification.NotificationAdapter.NotificationViewHolder
import com.example.yeongkkuel.presentation.my.notification.data.NotificationListItem
import com.example.yeongkkuel.presentation.my.reward.data.RewardItem
import com.example.yeongkkuel.presentation.my.reward.data.RewardListItem
import com.example.yeongkkuel.presentation.my.reward.data.RewardType

class RewardAdapter (
    private var displayItems: List<RewardListItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayItems[position]) {
            is RewardListItem.HeaderItem -> VIEW_TYPE_HEADER
            is RewardListItem.NormalItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_reward_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_reward, parent, false)
                RewardViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = displayItems[position]) {
            is RewardListItem.HeaderItem -> {
                (holder as HeaderViewHolder).bind(item)
            }
            is RewardListItem.NormalItem -> {
                (holder as RewardViewHolder ).bind(item.notification)
            }
        }
    }

    override fun getItemCount(): Int = displayItems.size


    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        private val viewHeaderDivider: View? = itemView.findViewById(R.id.viewHeaderDivider)

        fun bind(headerItem: RewardListItem.HeaderItem) {
            tvHeader.text = headerItem.sectionName

            viewHeaderDivider?.visibility = if (adapterPosition == 0) View.GONE else View.VISIBLE
        }
    }

    class RewardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIcon = itemView.findViewById<ImageView>(R.id.ivRewardIcon)
        private val tvMessage = itemView.findViewById<TextView>(R.id.tvRewardMessage)
        private val reward = itemView.findViewById<TextView>(R.id.tvReward)

        fun bind(item: RewardItem) {
            // 아이콘은 NotificationType에 따라 다른 drawable을 설정
            val iconRes = when (item.type) {
                RewardType.TEAM_GOAL -> R.drawable.ic_team_goal
                RewardType.GOAL -> R.drawable.ic_no_spend_reward
                else -> R.drawable.ic_no_spend_reward
            }
            // type 에따라 tvmessage 색상 변경

            when (item.type) {
                RewardType.TEAM_GOAL -> reward.setTextColor(ContextCompat.getColor(itemView.context, R.color.sub1))
                RewardType.GOAL -> reward.setTextColor(ContextCompat.getColor(itemView.context, R.color.main1))
                else -> reward.setTextColor(ContextCompat.getColor(itemView.context, R.color.sub2))
            }


            ivIcon.setImageResource(iconRes)

            // 점수, 메시지 반영
            tvMessage.text = item.message
            reward.text = "+ " + item.rewardText


        }
    }

}