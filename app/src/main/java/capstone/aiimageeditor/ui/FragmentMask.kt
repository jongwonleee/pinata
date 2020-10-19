package capstone.aiimageeditor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.customviews.DrawingView
import capstone.aiimageeditor.databinding.FragmentMaskBinding
import com.google.android.material.tabs.TabLayout


class FragmentMask:BaseKotlinFragment<FragmentMaskBinding>(){
    override val layoutResourceId: Int
        get() = R.layout.fragment_mask

    private lateinit var imageManager: ImageManager
    private lateinit var maskView: DrawingView

    override fun initStartView() {
        imageManager = (requireActivity().application as ImageManager)
        setImage(requireContext())
    }

    //TODO custom zoomable view 이용해 줌 기능 추가
    override fun initDataBinding() {
        binding.seekBar.max = 90
        binding.seekBar.progress = 45
    }

    override fun initAfterBinding() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                maskView.setStrokeWidth((p1 + 10).toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> maskView.setBrush(true)
                    else -> maskView.setBrush(false)
                }
            }
        })
    }

    override fun reLoadUI() {
    }

    fun setImage(context: Context) {
        maskView = DrawingView(context, imageManager.mask, imageManager.original)
        maskView.setStrokeWidth(55f)
        binding.imageBg.setImageBitmap(imageManager.original)
        binding.imageFg.addView(maskView)
    }

    fun deleteView() {
        imageManager.mask = maskView.mask
        binding.imageFg.removeView(maskView)
    }
}
