package com.wstxda.toolkit.ui.component

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wstxda.toolkit.R
import com.wstxda.toolkit.services.UpdaterService
import com.wstxda.toolkit.ui.utils.Haptics
import com.wstxda.toolkit.viewmodel.AboutAppViewModel
import com.google.android.material.divider.MaterialDivider
import androidx.core.widget.NestedScrollView
import com.wstxda.toolkit.databinding.DialogAboutAppBinding
import com.wstxda.toolkit.ui.adapter.AboutAppAdapter

class AboutAppBottomSheet : BaseBottomSheet<DialogAboutAppBinding>() {

    private lateinit var haptics: Haptics
    private val viewModel: AboutAppViewModel by viewModels()

    override val dividerTop: MaterialDivider get() = binding.dividerTop
    override val dividerBottom: MaterialDivider get() = binding.dividerBottom
    override val scrollView: NestedScrollView get() = binding.scrollView

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        DialogAboutAppBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        haptics = Haptics(requireContext().applicationContext)

        val adapter = AboutAppAdapter(viewModel::openUrl)
        binding.recyclerLinks.adapter = adapter

        viewModel.applicationVersion.observe(viewLifecycleOwner) { version ->
            binding.appUpdate.text = getString(R.string.about_version, version)

            binding.appUpdate.setOnClickListener {
                UpdaterService.checkForUpdates(requireContext(), it)
            }

            binding.appIconContainer.setOnClickListener {
                haptics.tick()
                viewModel.openAppInfo()
            }
        }

        viewModel.links.observe(viewLifecycleOwner) { links ->
            adapter.submitList(links)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val behavior = BottomSheetBehavior.from(requireView().parent as View)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }
}