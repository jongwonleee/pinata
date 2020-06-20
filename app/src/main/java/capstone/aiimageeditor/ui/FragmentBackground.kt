package capstone.aiimageeditor.ui

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import java.lang.Exception


class FragmentBackground : Fragment() {


    private lateinit var seekBar: SeekBar
    private lateinit var imageBG: ImageView
    private lateinit var imageFG: ImageView
    private lateinit var gpuImage: GPUImage
    private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager
    private lateinit var maskSeparator: MaskSeparator

    private var tabPosition = 0
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        gpuImage = GPUImage(context)
        tabLayout = view.findViewById(R.id.tabLayout)

        maskSeparator = MaskSeparator()
        imageManager = (activity?.application as ImageManager)
        imageManager.backgroundAdjusts = imageManager.backgroundAdjusts
        imageManager.backgroundFilters = imageManager.backgroundFilters

        imageBG.visibility = View.VISIBLE
        seekBar.max = 100

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (imageManager.backgroundFilters[tabPosition] != null) {
                    imageManager.backgroundAdjusts[tabPosition] = progress
                    filterAdjuster = GPUImageFilterTools.FilterAdjuster(imageManager.backgroundFilters[tabPosition]!!)
                    filterAdjuster?.adjust(progress)
                    imageBG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters))

                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tabLayout.addOnTabSelectedListener(tabListener)
        setImage()
        addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.BRIGHTNESS))
        seekBar.progress = 50
    }

    fun setImage() {
        try {
            gpuImage.setImage(imageManager.backgroundOriginal)
            imageFG.setImageBitmap(imageManager.personFiltered)
            imageBG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters))

        } catch (e: Exception) {

            e.printStackTrace()
        }

    }

    fun saveImage() {
        imageManager.backgroundFiltered = gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_background, container, false)
    }


    private fun addFilter(f: GPUImageFilter) {
        val index = tabPosition
        var filter = f
        if (imageManager.backgroundFilters[index] != null) {
            filter = imageManager.backgroundFilters[index]!!
            seekBar.progress = imageManager.backgroundAdjusts[index];
        } else {
            imageManager.backgroundFilters[index] = f
        }

        filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter)
        if (filterAdjuster!!.canAdjust()) {
            seekBar.visibility = View.VISIBLE
        } else {
            seekBar.visibility = View.GONE
        }
    }/*
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
            imageBG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters))
            seekBar.visibility = View.VISIBLE
            tabPosition = tab!!.position
            when (tab?.position) {
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
            seekBar.progress = imageManager.backgroundAdjusts[tabPosition]
        }

    }

}
