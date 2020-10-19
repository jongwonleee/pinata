package capstone.aiimageeditor.ui


import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageHalo
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.databinding.FragmentPersonBinding
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import kotlinx.android.synthetic.main.fragment_person.*
import yuku.ambilwarna.AmbilWarnaDialog

//밝기 내리는거, 대조 올리는거 잘됨
//밝기 올리는거, 대조 내리는거 잘 안됨
//톤은 컨트롤바가 안 나옴
//채도는 컨트롤바 만져도 안 바뀜
//틴트하면 배경이 까맣게 바뀌고 틴트를 최대치로 하면 인물도 까맣게 됨

class FragmentPerson : BaseKotlinFragment<FragmentPersonBinding>(), View.OnClickListener, View.OnTouchListener{
    override val layoutResourceId: Int
        get() = R.layout.fragment_person

    private lateinit var gpuImage: GPUImage
    private lateinit var imageManager: ImageManager
    private lateinit var imageHalo: ImageHalo
    private var tabPosition = 0
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null
    private var isLiquify = true

    override fun initStartView() {
        imageManager = (activity?.application as ImageManager)
        gpuImage = GPUImage(context)

        binding.buttonColorChange.visibility = View.GONE
        binding.imageBg.visibility = View.VISIBLE

        binding.seekBar.max = 100
        binding.seekBar.visibility = View.GONE
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
        binding.buttonColorChange.setOnClickListener(this)
        binding.buttonTogleLiquify.setOnClickListener(this)
        binding.imageFg.setOnTouchListener(this)
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                when (tabPosition) {
                    0 -> {
                        if (isLiquify) binding.viewLiquifyview.brushSizeChange(progress / 25) //0~4
                        else binding.viewTallview.tall(progress.toFloat() / 33 / 10 + 1.0f) //1.00f ~ 1.30f
                    }
                    8 -> {
                        if (seekBar != null) {
                            if (seekBar.progress == 0) imageManager.doHalo = false
                            else {
                                imageManager.doHalo = true
                                imageManager.personAdjusts[8] = seekBar.progress
                                imageHalo.setWeight(seekBar.progress)
                            }
                        }
                        applyFilters(true)
                    }
                    else -> {
                        imageManager.personAdjusts[tabPosition] = progress
                        filterAdjuster = GPUImageFilterTools.FilterAdjuster(imageManager.personFilters[tabPosition]!!)
                        filterAdjuster?.adjust(progress)
                        applyFilters(true)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.tabLayout.addOnTabSelectedListener(tabListener)
    }

    override fun reLoadUI() {
    }

    override fun onClick(p0: View?) {
        if (tabPosition == 0) {
            if (isLiquify) {
                binding.viewLiquifyview.visibility = View.GONE
                imageManager.personOriginal = binding.viewLiquifyview.getLiquifiedImage(imageManager.original.width, imageManager.original.height)
                binding.buttonTogleLiquify.setImageResource(R.drawable.ic_finger)
            } else {
                //binding.viewTallview.removeLines()
                binding.viewTallview.visibility = View.GONE

                imageManager.personOriginal = binding.viewTallview.getTalledImage(
                    imageManager.original.width,
                    imageManager.original.height
                )
                binding.buttonTogleLiquify.setImageResource(R.drawable.ic_height)
            }
            isLiquify = !isLiquify
            setLiquify()
        } else {
            val colorPicker = AmbilWarnaDialog(view?.context, Color.RED, true, object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    return
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    imageHalo.setColor(color)
                    applyFilters(true)
                }
            })
            colorPicker.show()
        }

    }

    override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
        when (p1?.action) {
            MotionEvent.ACTION_DOWN -> white_view.visibility = View.VISIBLE
            MotionEvent.ACTION_UP -> white_view.visibility = View.GONE
        }
        return true
    }

    fun refreshBackground() {
        binding.imageBg.setImageBitmap(gpuImage.getBitmapWithFiltersApplied(imageManager.backgroundOriginal, imageManager.backgroundFilters))
    }

    fun setImage() {
        binding.imageBg.setImageBitmap(imageManager.backgroundFiltered)
        binding.viewLiquifyview.setup(30, 50, imageManager.personOriginal, imageManager.backgroundOriginal)
        binding.viewTallview.setup(1, 50, imageManager.personOriginal, imageManager.backgroundOriginal)
        imageHalo = ImageHalo()
        imageHalo.setWeight(imageManager.personAdjusts[8])
        imageHalo.setColor(imageManager.haloColor)
        binding.viewLiquifyview.visibility = View.VISIBLE
        binding.seekBar.visibility = View.VISIBLE
        binding.imageFg.setImageBitmap(imageManager.personFiltered)
        binding.tabLayout.getTabAt(0)?.select()
    }

    fun saveImage() {
        if (tabPosition == 0) {
            if (isLiquify) imageManager.personOriginal =
                binding.viewLiquifyview.getLiquifiedImage(imageManager.original.width, imageManager.original.height)
            else imageManager.personOriginal = binding.viewTallview.getTalledImage(imageManager.original.width, imageManager.original.height)

        }
        applyFilters(false)
    }


    private fun addFilter(f: GPUImageFilter) {
        binding.imageFg.visibility = View.VISIBLE
        val index = tabPosition
        var filter = f
        if (imageManager.personFilters[index] != null) {
            filter = imageManager.personFilters[index]!!
            binding.seekBar.progress = imageManager.personAdjusts[index]
        } else {
            imageManager.personFilters[index] = f
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
        override fun onTabReselected(tab: TabLayout.Tab?) {
            when (tab?.position) {
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> {
                    if (isLiquify) {
                        binding.viewLiquifyview.visibility = View.GONE
                        imageManager.personOriginal =
                            binding.viewLiquifyview.getLiquifiedImage(imageManager.original.width, imageManager.original.height)
                        binding.buttonTogleLiquify.setImageResource(R.drawable.ic_finger)
                    } else {
                        binding.viewTallview.visibility = View.GONE

                        imageManager.personOriginal = binding.viewTallview.getTalledImage(
                            imageManager.original.width,
                            imageManager.original.height
                        )
                        binding.buttonTogleLiquify.setImageResource(R.drawable.ic_height)
                    }
                    isLiquify = true
                }
            }
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {


            applyFilters(true)

            binding.buttonColorChange.visibility = View.GONE
            binding.seekBar.visibility = View.VISIBLE
            tabPosition = tab!!.position
            binding.viewLiquifyview.visibility = View.GONE
            binding.viewTallview.visibility = View.GONE
            binding.imageFg.visibility = View.GONE
            white_view.visibility = View.GONE
            binding.buttonTogleLiquify.visibility = View.GONE

            when (tab.position) {
                0 -> {
                    binding.buttonTogleLiquify.visibility = View.VISIBLE
                    setLiquify()
                }
                1 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.GAMMA))
                2 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.SATURATION))
                3 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.EXPOSURE))
                4 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.HIGHLIGHT_SHADOW))
                5 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.WHITE_BALANCE))
                6 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.HAZE))
                7 -> addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.VIBRANCE))
                8 -> {
                    binding.imageFg.visibility = View.VISIBLE
                    binding.buttonColorChange.visibility = View.VISIBLE
                    imageHalo.setColor(imageManager.haloColor)
                    binding.seekBar.progress = 0
                    applyFilters(true)
                }
                9 -> {
                    addFilter(GPUImageFilterTools.createFilterForType(context!!, GPUImageFilterTools.FilterType.TOON))
                }
            }
            binding.seekBar.progress = imageManager.personAdjusts[tabPosition]
        }

    }

    fun applyFilters(toImageView: Boolean) {
        var bitmap = Bitmap.createBitmap(imageManager.personOriginal)
        bitmap = gpuImage.getBitmapWithFiltersApplied(bitmap, imageManager.personFilters)
        Log.i("!!", "${imageManager.doHalo}")
        if (imageManager.doHalo) setImage(toImageView, imageHalo.run(bitmap))
        else {
            setImage(toImageView, bitmap)
        }

    }

    private fun setImage(toImageView: Boolean, bitmap: Bitmap) {
        if (toImageView) binding.imageFg.setImageBitmap(bitmap)
        imageManager.personFiltered = bitmap
    }


    fun setLiquify() {
        binding.viewLiquifyview.visibility = View.GONE
        binding.viewTallview.visibility = View.GONE
        binding.imageFg.visibility = View.GONE
        white_view.visibility = View.GONE
        if (isLiquify) {
            binding.viewLiquifyview.setup(30, 50, imageManager.personOriginal, imageManager.backgroundOriginal)
            binding.viewLiquifyview.visibility = View.VISIBLE
            binding.seekBar.progress = 0
            binding.seekBar.visibility = View.VISIBLE
            white_view.visibility = View.VISIBLE
        } else {
            binding.viewTallview.setup(1, 50, imageManager.personOriginal, imageManager.backgroundOriginal)
            binding.viewTallview.visibility = View.VISIBLE
            white_view.visibility = View.VISIBLE

            binding.seekBar.progress = 0
        }
    }
}