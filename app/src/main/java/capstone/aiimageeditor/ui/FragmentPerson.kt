package capstone.aiimageeditor.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageHalo
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

//밝기 내리는거, 대조 올리는거 잘됨
//밝기 올리는거, 대조 내리는거 잘 안됨
//톤은 컨트롤바가 안 나옴
//채도는 컨트롤바 만져도 안 바뀜
//틴트하면 배경이 까맣게 바뀌고 틴트를 최대치로 하면 인물도 까맣게 됨

class FragmentPerson : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG: ImageView
    private lateinit var imageFG: ImageView
    private lateinit var gpuImage: GPUImage
    private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager
    private lateinit var imageHalo: ImageHalo

    private var filters = arrayListOf<GPUImageFilter?>()
    private var adjusts = arrayListOf<Int>()
    private var tabPosition = 0
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (i in 0..8) {
            filters.add(null)
            adjusts.add(50)
        }
        adjusts[4] = 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
//        btnHalo = view.findViewById(R.id.btnHalo)
        tabLayout = view.findViewById(R.id.tabLayout)

        imageManager = (activity?.application as ImageManager)
        imageHalo = ImageHalo()

        gpuImage = GPUImage(context)

        imageBG.visibility = View.VISIBLE
        seekBar.max = 100
        seekBar.progress = 50
        seekBar.visibility = View.GONE

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                adjusts[tabPosition] = progress
                filterAdjuster = GPUImageFilterTools.FilterAdjuster(filters[tabPosition]!!)
                filterAdjuster?.adjust(progress)
                //gpuImage.requestRender()
                imageFG.setImageBitmap(
                    gpuImage.getBitmapWithFiltersApplied(
                        imageManager.personOriginal,
                        filters
                    )
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tabLayout.addOnTabSelectedListener(tabListener)
    }

    public fun setImage() {
        imageFG.setImageBitmap(imageManager.personOriginal)
        imageBG.setImageBitmap(imageManager.backgroundFiltered)
        gpuImage.setImage(imageManager.personOriginal)

    }

    public fun saveImage() {
        imageManager.personFiltered =
            gpuImage.getBitmapWithFiltersApplied(imageManager.personOriginal, filters)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false)
    }


    private fun addFilter(f: GPUImageFilter, index: Int) {
        var filter = f
        if (filters[index] != null) {
            filter = filters[index]!!
            seekBar.progress = adjusts[index];
        } else {
            filters[index] = f
        }

        filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter)
        if (filterAdjuster!!.canAdjust()) {
            seekBar.visibility = View.VISIBLE
        } else {
            seekBar.visibility = View.GONE
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
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            imageFG.setImageBitmap(
                gpuImage.getBitmapWithFiltersApplied(
                    imageManager.personOriginal,
                    filters
                )
            )
            seekBar.visibility = View.VISIBLE
            tabPosition = tab!!.position
            when (tab?.position) {
                0 -> {
                    imageFG.setImageBitmap(imageManager.personOriginal)
                    seekBar.visibility = View.GONE
                }
                1 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.GAMMA
                    ), tab?.position
                )
                2 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.SATURATION
                    ), tab?.position
                )
                3 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.EXPOSURE
                    ), tab?.position
                )
                4 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.HIGHLIGHT_SHADOW
                    ), tab?.position
                )
                5 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.WHITE_BALANCE
                    ), tab?.position
                )
                6 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.HAZE
                    ), tab?.position
                )
                7 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.VIBRANCE
                    ), tab?.position
                )
                8 -> {
//                    imageManager.personOriginal =
//                        imageHalo.setHalo(imageManager.personOriginal, imageManager.mask)
                    val mutableImagePerson = imageManager.original.copy(Bitmap.Config.ARGB_8888,true)
                    val mutableImageMask = imageManager.mask.copy(Bitmap.Config.ARGB_8888,true)
                    imageManager.backgroundFiltered =
                        imageHalo.setHalo(mutableImagePerson, mutableImageMask)
                    seekBar.visibility = View.GONE
                    setImage()
                }
            }
            seekBar.progress = adjusts[tabPosition]
            //gpuImage.requestRender()
        }

    }

}
