package capstone.aiimageeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.databinding.FragmentBackgroundBinding
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

class FragmentBackground : BaseKotlinFragment<FragmentBackgroundBinding>(){
    override val layoutResourceId: Int
        get() = R.layout.fragment_background

    private lateinit var gpuImage: GPUImage
    private lateinit var imageManager: ImageManager
    private lateinit var maskSeparator: MaskSeparator

    private var tabPosition = 0
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null


    override fun initStartView() {
        gpuImage = GPUImage(context)
        maskSeparator = MaskSeparator()
        imageManager = (activity?.application as ImageManager)
        imageManager.backgroundAdjusts = imageManager.backgroundAdjusts
        imageManager.backgroundFilters = imageManager.backgroundFilters

        binding.imageBg.visibility = View.VISIBLE
        binding.seekBar.max = 100
        binding.seekBar.progress = 50

    }

    override fun initDataBinding() {
        setImage()
        addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.BRIGHTNESS))
    }

    override fun initAfterBinding() {
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (imageManager.backgroundFilters[tabPosition] != null) {
                    imageManager.backgroundAdjusts[tabPosition] = progress
                    filterAdjuster = GPUImageFilterTools.FilterAdjuster(imageManager.backgroundFilters[tabPosition]!!)
                    filterAdjuster?.adjust(progress)
                    binding.imageBg.setImageBitmap(
                        gpuImage.getBitmapWithFiltersApplied(
                            imageManager.backgroundOriginal,
                            imageManager.backgroundFilters
                        )
                    )

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.tabLayout.addOnTabSelectedListener(tabListener)
    }

    override fun reLoadUI() {
    }

    fun setImage() {
        try {
            gpuImage.setImage(imageManager.backgroundOriginal)
            binding.imageFg.setImageBitmap(imageManager.personFiltered)
            binding.imageBg.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters))

        } catch (e: Exception) {

            e.printStackTrace()
        }

    }

    fun saveImage() {
        imageManager.backgroundFiltered = gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters)
    }

    private fun addFilter(f: GPUImageFilter) {
        val index = tabPosition
        var filter = f
        if (imageManager.backgroundFilters[index] != null) {
            filter = imageManager.backgroundFilters[index]!!
            binding.seekBar.progress = imageManager.backgroundAdjusts[index]
        } else {
            imageManager.backgroundFilters[index] = f
        }

        filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter)
        if (filterAdjuster!!.canAdjust()) {
            binding.seekBar.visibility = View.VISIBLE
        } else {
            binding.seekBar.visibility = View.GONE
        }
    }

    /*
    brightness 45~55
    contrast 40~60
    gamma 40~60
    sharpness 40~60
    saturation 0~100
    exposure 45~55
    highlight 0~50
    whitebalance 30~70
    vinette 50~70
    gausian blur -> 흐리
    Toon, SmoothToon -> 만화효과
    haze 40~60
    vibrance 25~75
    */

    val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            binding.imageBg.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters))
            binding.seekBar.visibility = View.VISIBLE
            tabPosition = tab!!.position
            when (tab.position) {
                0 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.BRIGHTNESS))
                1 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.CONTRAST))
                2 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.SHARPEN))
                3 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.SATURATION))
                4 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.EXPOSURE))
                5 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.HIGHLIGHT_SHADOW))
                6 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.WHITE_BALANCE))
                7 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.HAZE))
                8 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.VIBRANCE))

            }
            binding.seekBar.progress = imageManager.backgroundAdjusts[tabPosition]
        }
    }
}
