package capstone.aiimageeditor.ui

import android.R.attr.button
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter


class FragmentPerson : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG: ImageView
    private lateinit var imageFG: GPUImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager
    private lateinit var maskSeparator: MaskSeparator

    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        tabLayout = view.findViewById(R.id.tabLayout)

        maskSeparator = MaskSeparator()
        imageManager = (activity?.application as ImageManager)

        imageFG.setScaleType(GPUImage.ScaleType.CENTER_INSIDE)
        imageFG.setBackgroundColor(255f,255f,255f)
        seekBar.max=100
        seekBar.progress=50
        seekBar.visibility=View.GONE

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                filterAdjuster?.adjust(progress)
                imageFG.requestRender()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        tabLayout.addOnTabSelectedListener(tabListener)
    }


    public fun setImage(){
        imageFG.setImage(imageManager.person)
        imageBG.setImageBitmap(imageManager.background)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false)
    }


    private fun switchFilterTo(filter: GPUImageFilter) {
        if (imageFG.filter == null || imageFG.filter.javaClass != filter.javaClass) {
            imageFG.filter = filter
            filterAdjuster = GPUImageFilterTools.FilterAdjuster(filter)
            if (filterAdjuster!!.canAdjust()) {
                seekBar.visibility = View.VISIBLE
                filterAdjuster!!.adjust(seekBar.progress)
            } else {
                seekBar.visibility = View.GONE
            }
        }
    }

    val tabListener = object : TabLayout.OnTabSelectedListener{
        override fun onTabReselected(tab: TabLayout.Tab?) {
            imageManager.person = maskSeparator.applyWithMask(imageManager.original,imageManager.mask)
            imageManager.background = maskSeparator.applyWithoutMask(imageManager.original,imageManager.mask)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            imageManager.person = maskSeparator.applyWithMask(imageManager.original,imageManager.mask)
            imageManager.background = maskSeparator.applyWithoutMask(imageManager.original,imageManager.mask)

            seekBar.visibility=View.VISIBLE
            seekBar.progress=50
            when(tab?.position){
                0->{
                    imageFG.filter=null
                    seekBar.visibility=View.GONE
                }
                1-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.BRIGHTNESS))
                2-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.CONTRAST))
                3-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.TONE_CURVE))
                //4-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.))
                5-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.HUE))
                6-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.VIGNETTE))
                7-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.SHARPEN))
                8-> switchFilterTo(GPUImageFilterTools.createFilterForType(context!!,GPUImageFilterTools.FilterType.GAMMA))
            }
            imageFG.requestRender()

        }

    }

}
