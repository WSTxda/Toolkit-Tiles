package com.wstxda.toolkit.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.listitem.ListItemLayout
import com.wstxda.toolkit.data.AboutItem
import com.wstxda.toolkit.databinding.ListItemAboutBinding
import com.wstxda.toolkit.ui.utils.Haptics

class AboutAppAdapter(
    private val onUrlClick: (AboutItem) -> Unit,
    private val onActionClick: (AboutItem) -> Unit,
) : ListAdapter<AboutItem, AboutAppAdapter.LinkViewHolder>(LinkDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LinkViewHolder(
        ListItemAboutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(getItem(position), position, itemCount)
    }

    inner class LinkViewHolder(private val binding: ListItemAboutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val haptics = Haptics(itemView.context.applicationContext)

        fun bind(link: AboutItem, position: Int, totalItems: Int) = with(binding) {
            link.title?.let { titleItem.setText(it) }
            titleItem.isVisible = link.title != null

            link.icon?.let { iconItem.setImageResource(it) }
            iconItem.isVisible = link.icon != null

            link.summary?.let { summaryItem.setText(it) }
            summaryItem.isVisible = link.summary != null

            val isClickable = link.url != null || link.isActionItem
            cardItem.isClickable = isClickable
            cardItem.isFocusable = isClickable

            if (isClickable) {
                cardItem.setOnClickListener {
                    haptics.low()
                    if (link.isActionItem) onActionClick(link) else onUrlClick(link)
                }
            } else {
                cardItem.setOnClickListener(null)
            }

            val listItemLayout = itemView as ListItemLayout
            listItemLayout.updateAppearance(position, totalItems)
        }
    }
}