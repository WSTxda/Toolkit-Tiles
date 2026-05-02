package com.wstxda.toolkit.ui.component

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.divider.MaterialDivider
import com.wstxda.toolkit.R
import com.wstxda.toolkit.databinding.DialogAboutAppBinding
import com.wstxda.toolkit.services.UpdaterService
import com.wstxda.toolkit.ui.adapter.AboutAppAdapter
import com.wstxda.toolkit.ui.utils.Haptics
import com.wstxda.toolkit.viewmodel.AboutAppViewModel

class AboutAppBottomSheet : BaseBottomSheet<DialogAboutAppBinding>() {

    private lateinit var haptics: Haptics
    private val viewModel: AboutAppViewModel by viewModels()

    override val topDivider: MaterialDivider get() = binding.dividerTop
    override val bottomDivider: MaterialDivider get() = binding.dividerBottom
    override val scrollView: NestedScrollView get() = binding.scrollView
    override val defaultExpanded: Boolean = true

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogAboutAppBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        haptics = Haptics(requireContext().applicationContext)

        val adapter = AboutAppAdapter(viewModel::openUrl)
        binding.dialogRecyclerLinks.adapter = adapter

        viewModel.applicationVersion.observe(viewLifecycleOwner) { version ->
            binding.dialogButtonUpdate.text = getString(R.string.about_version, version)

            binding.dialogButtonUpdate.setOnClickListener {
                haptics.low()
                UpdaterService.checkForUpdates(
                    scope = lifecycleScope,
                    context = requireContext(),
                    fragmentManager = parentFragmentManager,
                    anchorView = it
                )
            }

            binding.dialogIconContainer.setOnClickListener {
                haptics.low()
                viewModel.openAppInfo()
            }
        }

        viewModel.links.observe(viewLifecycleOwner) { links ->
            adapter.submitList(links)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }
}