package capstone.aiimageeditor.ui

import android.view.View
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import com.google.android.material.tabs.TabLayout

abstract class GPUImageUI {
    val tabListener = object : TabLayout.OnTabSelectedListener{
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {}

        override fun onTabSelected(tab: TabLayout.Tab?) {
            imageFG.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.person,filters))
            seekBar.visibility= View.VISIBLE
            tabPosition= tab!!.position
            when(tab?.position){
                0->{
                    imageFG.setImageBitmap(imageManager.person)
                    seekBar.visibility= View.GONE
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
            //gpuImage.requestRender()
        }

    }
}