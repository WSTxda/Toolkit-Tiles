package com.wstxda.toolkit.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.wstxda.toolkit.data.AboutItem

object LinkDiffCallback : DiffUtil.ItemCallback<AboutItem>() {

    override fun areItemsTheSame(oldItem: AboutItem, newItem: AboutItem) =
        oldItem.title == newItem.title

    override fun areContentsTheSame(oldItem: AboutItem, newItem: AboutItem) = oldItem == newItem
}