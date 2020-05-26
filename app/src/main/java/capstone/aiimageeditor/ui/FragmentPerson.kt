package capstone.aiimageeditor.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.customviews.DrawingView
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter


class FragmentPerson : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG: ImageView
    private lateinit var imageFG: ImageView
    private lateinit var gpuImage: GPUImage
    private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager
    private lateinit var maskSeparator: MaskSeparator
    private var filters = arrayListOf<GPUImageFilter?>()

    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for(i in 0 .. 8) filters.add(null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        tabLayout = view.findViewById(R.id.tabLayout)
        maskSeparator = MaskSeparator()
        imageManager = (activity?.application as ImageManager)

        gpuImage = GPUImage(context)

        /*imageFG.setScaleType(GPUImage.ScaleType.CENTER_INSIDE)
        imageFG.setBackgroundColor(Color.TRANSPARENT)*/
        imageBG.visibility=View.VISIBLE
        seekBar.max=100
        seekBar.progress=50
        seekBar.visibility=View.GONE

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                filterAdjuster?.adjust(progress)
                //gpuImage.requestRender()
                imageFG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.person,filters))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tabLayout.addOnTabSelectedListener(tabListener)
    }

    public fun setImage(){
        imageFG.setImageBitmap(imageManager.person)
        imageBG.setImageBitmap(imageManager.original)
        gpuImage.setImage(imageManager.person)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false)
    }


    private fun addFilter(filter: GPUImageFilter,index:Int) {
        filters[index]= filter
        filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter)
        if (filterAdjuster!!.canAdjust()) {
            seekBar.visibility = View.VISIBLE
            filterAdjuster!!.adjust(seekBar.progress)
        } else {
            seekBar.visibility = View.GONE
        }
    }

    val tabListener = object : TabLayout.OnTabSelectedListener{
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {

            seekBar.visibility=View.VISIBLE
            seekBar.progress=50
            when(tab?.position){
                0->{
                    gpuImage.setFilter(null)
                    seekBar.visibility=View.GONE
                }
                1-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.BRIGHTNESS),1)
                2-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.CONTRAST),2)
                3-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.TONE_CURVE),3)
                //4-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.))
                5-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.HUE),5)
                6-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.VIGNETTE),6)
                7-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.SHARPEN),7)
                8-> addFilter(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.GAMMA),8)
            }
            //gpuImage.requestRender()
            imageFG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.person,filters))
        }

    }

}
