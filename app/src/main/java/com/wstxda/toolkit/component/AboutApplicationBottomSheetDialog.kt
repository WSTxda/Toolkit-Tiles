package com.wstxda.toolkit.component

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wstxda.toolkit.R
import com.wstxda.toolkit.adapter.AboutApplicationAdapter
import com.wstxda.toolkit.databinding.BottomSheetAboutBinding
import com.wstxda.toolkit.services.UpdaterService
import com.wstxda.toolkit.ui.utils.Haptics
import com.wstxda.toolkit.viewmodel.AboutApplicationViewModel

class AboutApplicationBottomSheetDialog : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAboutBinding? = null
    private lateinit var haptics: Haptics
    private val binding get() = _binding!!
    private val viewModel: AboutApplicationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        haptics = Haptics(requireContext().applicationContext)

        val adapter = AboutApplicationAdapter(viewModel::openUrl)
        binding.recyclerLinks.adapter = adapter

        binding.appIconShape.animate().rotationBy(360f).setDuration(20_000)
            .setInterpolator(LinearInterpolator())
            .withEndAction { binding.appIconShape.animate().rotationBy(360f).start() }.start()

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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.appIconShape.animate().cancel()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.finish()
    }
}