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
import capstone.aiimageeditor.ZoomGestureListener
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout


class FragmentMask : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG: ImageView
    private lateinit var imageFG: LinearLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager
    private lateinit var maskView: DrawingView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekBar = view.findViewById(R.id.seekBar)
        imageBG = view.findViewById(R.id.image_bg)
        imageFG = view.findViewById(R.id.image_fg)
        tabLayout = view.findViewById(R.id.tabLayout)
        imageManager = (activity?.application as ImageManager)

        setImage(context!!)

        seekBar.max = 90
        seekBar.progress = 45
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                maskView.setStrokeWidth((p1 + 10).toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
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

    fun setImageBitmap(iv: ImageView, bitmap: Bitmap) {
        Glide.with(this).load(bitmap).into(iv)
    }

    fun setImage(context: Context) {
        maskView = DrawingView(context, imageManager.mask, imageManager.original)
        maskView.setStrokeWidth(55f)
        setImageBitmap(imageBG, imageManager.original)
        imageFG.addView(maskView)
    }

    fun deleteView() {
        imageManager.mask = maskView.mask
        imageFG.removeView(maskView)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mask, container, false)
    }

}
