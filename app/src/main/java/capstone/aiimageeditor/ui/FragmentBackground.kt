package capstone.aiimageeditor.ui

import android.os.Bundle
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
    private var filters = arrayListOf<GPUImageFilter?>()
    private var adjusts = arrayListOf<Int>()
    private var tabPosition=0
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for(i in 0 .. 9) {
            filters.add(null)
            adjusts.add(50)
        }
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


        imageBG.visibility=View.VISIBLE
        seekBar.max=100
        seekBar.progress=50
        seekBar.visibility=View.GONE

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(filters[tabPosition]!=null){
                    adjusts[tabPosition]=progress
                    filterAdjuster = GPUImageFilterTools.FilterAdjuster(filters[tabPosition]!!)
                    filterAdjuster?.adjust(progress)
                    imageBG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.background,filters))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tabLayout.addOnTabSelectedListener(tabListener)
        setImage()
    }


    public fun setImage()  {
        try{
            imageFG.setImageBitmap(imageManager.person)
            imageBG.setImageBitmap(imageManager.background)
            gpuImage.setImage(imageManager.background)
        }catch (e:Exception){

        }

    }
    public fun saveImage(){
        imageManager.background = gpuImage.getBitmapWithFiltersApplied(imageManager.background,filters)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_background, container, false)
    }


    private fun addFilter(f: GPUImageFilter, index:Int) {
        var filter = f
        if(filters[index]!=null){
            filter = filters[index]!!
            seekBar.progress = adjusts[index];
        }else
        {
            filters[index]=f
        }

        filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter)
        if (filterAdjuster!!.canAdjust()) {
            seekBar.visibility = View.VISIBLE
        } else {
            seekBar.visibility = View.GONE
        }
    }

    val tabListener = object : TabLayout.OnTabSelectedListener{
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            imageBG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.background,filters))
            seekBar.visibility=View.VISIBLE
            tabPosition= tab!!.position
            when(tab?.position){
                0->{
                    imageFG.setImageBitmap(imageManager.person)
                    seekBar.visibility=View.GONE
                }
                1-> addFilter(
                    GPUImageFilterTools.createFilterForType(context!!,
                        GPUImageFilterTools.FilterType.BRIGHTNESS),1)
                2-> addFilter(
                    GPUImageFilterTools.createFilterForType(context!!,
                        GPUImageFilterTools.FilterType.CONTRAST),2)
                3-> addFilter(
                    GPUImageFilterTools.createFilterForType(context!!,
                        GPUImageFilterTools.FilterType.TONE_CURVE),3)
                //4-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.))
                5-> addFilter(
                    GPUImageFilterTools.createFilterForType(context!!,
                        GPUImageFilterTools.FilterType.HUE),5)
                6-> addFilter(
                    GPUImageFilterTools.createFilterForType(context!!,
                        GPUImageFilterTools.FilterType.VIGNETTE),6)
                7-> addFilter(
                    GPUImageFilterTools.createFilterForType(context!!,
                        GPUImageFilterTools.FilterType.SHARPEN),7)
                8-> addFilter(
                    GPUImageFilterTools.createFilterForType(context!!,
                        GPUImageFilterTools.FilterType.GAMMA),8)
            }
            seekBar.progress=adjusts[tabPosition]
        }

    }

}
