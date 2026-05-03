package com.wstxda.toolkit.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.listitem.ListItemLayout
import com.mikepenz.aboutlibraries.entity.Library
import com.wstxda.toolkit.databinding.ListItemLibraryBinding
import com.wstxda.toolkit.ui.utils.Haptics

class LibraryAdapter(
    private val onClick: (Library) -> Unit
) : ListAdapter<Library, LibraryAdapter.LibraryViewHolder>(LibraryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = LibraryViewHolder(
        ListItemLibraryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) {
        holder.bind(getItem(position), position, itemCount)
    }

    inner class LibraryViewHolder(private val binding: ListItemLibraryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val haptics = Haptics(itemView.context.applicationContext)

        fun bind(library: Library, position: Int, totalItems: Int) = with(binding) {
            titleItem.text = library.name

            val licenseName = library.licenses.firstOrNull()?.name

            if (!licenseName.isNullOrEmpty()) {
                licenseItem.text = licenseName
                licenseItem.isVisible = true
            } else {
                licenseItem.isVisible = false
            }

            val version = library.artifactVersion
            if (!version.isNullOrEmpty()) {
                versionChip.text = version
                versionChip.isVisible = true
            } else {
                versionChip.isVisible = false
            }

            cardItem.setOnClickListener {
                haptics.low()
                onClick(library)
            }

            val listItemLayout = itemView as ListItemLayout
            listItemLayout.updateAppearance(position, totalItems)
        }
    }
}