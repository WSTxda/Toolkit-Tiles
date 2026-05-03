package com.wstxda.toolkit.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.mikepenz.aboutlibraries.entity.Library

object LibraryDiffCallback : DiffUtil.ItemCallback<Library>() {

    override fun areItemsTheSame(oldItem: Library, newItem: Library) =
        oldItem.uniqueId == newItem.uniqueId

    override fun areContentsTheSame(oldItem: Library, newItem: Library) = oldItem == newItem
}