@file:Suppress("DEPRECATION")

package com.wstxda.toolkit.ui.component

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import com.wstxda.toolkit.R
import com.wstxda.toolkit.databinding.DialogLibraryBinding
import com.wstxda.toolkit.ui.adapter.LibraryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

class LibraryBottomSheet : BaseBottomSheet<DialogLibraryBinding>() {

    private lateinit var libraryAdapter: LibraryAdapter

    override val topDivider: MaterialDivider get() = binding.dividerTop
    override val bottomDivider: MaterialDivider get() = binding.dividerBottom
    override val titleTextView: TextView get() = binding.dialogTitle
    override val titleResId: Int get() = R.string.about_used_library_summary

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogLibraryBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        libraryAdapter = LibraryAdapter { library ->
            library.website?.let { url ->
                if (url.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }
            }
        }

        binding.dialogRecyclerLibraries.adapter = libraryAdapter

        lifecycleScope.launch(Dispatchers.IO) {
            val libs = Libs.Builder().withContext(requireContext()).build()
            val librariesList = libs.libraries

            withContext(Dispatchers.Main) {
                libraryAdapter.submitList(librariesList)
            }
        }
    }

    override fun setupScrollListener() {
        binding.dialogRecyclerLibraries.post {
            updateDividerVisibility(
                canScrollUp = binding.dialogRecyclerLibraries.canScrollVertically(-1),
                canScrollDown = binding.dialogRecyclerLibraries.canScrollVertically(1)
            )
        }

        binding.dialogRecyclerLibraries.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateDividerVisibility(
                    canScrollUp = recyclerView.canScrollVertically(-1),
                    canScrollDown = recyclerView.canScrollVertically(1)
                )
            }
        })
    }
}