package capstone.aiimageeditor.ui

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.customviews.DrawingView
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.databinding.FragmentMaskBinding
import com.google.android.material.tabs.TabLayout


class FragmentMask : Fragment() {

    private lateinit var maskView: DrawingView
    private var _binding: FragmentMaskBinding?=null
    private val binding get()=_binding!!
    private lateinit var imageManager: ImageManager

//TODO custom zoomable view 이용해 줌 기능 추가
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    
        imageManager = (activity?.application as ImageManager)
        setImage(context!!)
        binding.seekBar.max = 90
        binding.seekBar.progress = 45
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMaskBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}
