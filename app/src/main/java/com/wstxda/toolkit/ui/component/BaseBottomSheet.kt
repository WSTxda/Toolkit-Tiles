package com.wstxda.toolkit.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.divider.MaterialDivider

abstract class BaseBottomSheet<VB : ViewBinding> : BottomSheetDialogFragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected abstract val topDivider: MaterialDivider
    protected abstract val bottomDivider: MaterialDivider
    protected abstract val scrollView: NestedScrollView
    protected open val titleTextView: TextView? = null
    protected open val titleResId: Int? = null

    protected abstract fun getBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = getBinding(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleResId?.let { resId -> titleTextView?.setText(resId) }
        setupScrollListener()
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupScrollListener() {
        scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            val canScrollUp = scrollView.canScrollVertically(-1)
            val canScrollDown = scrollView.canScrollVertically(1)
            updateDividerVisibility(canScrollUp, canScrollDown)
        }
    }

    protected fun updateDividerVisibility(canScrollUp: Boolean, canScrollDown: Boolean) {
        topDivider.isVisible = canScrollUp
        bottomDivider.isVisible = canScrollDown
    }
}