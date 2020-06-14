package capstone.aiimageeditor.ui


import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageHalo
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.customviews.LiquifyView
import capstone.aiimageeditor.imageprocessing.GPUImageFilterTools
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.lang.Exception
import yuku.ambilwarna.AmbilWarnaDialog

//밝기 내리는거, 대조 올리는거 잘됨
//밝기 올리는거, 대조 내리는거 잘 안됨
//톤은 컨트롤바가 안 나옴
//채도는 컨트롤바 만져도 안 바뀜
//틴트하면 배경이 까맣게 바뀌고 틴트를 최대치로 하면 인물도 까맣게 됨

class FragmentPerson : Fragment(),View.OnClickListener {

    private lateinit var seekBar: SeekBar
    private lateinit var imageBG: ImageView
    private lateinit var imageFG: ImageView
    private lateinit var imageLiquify: LiquifyView
    private lateinit var gpuImage: GPUImage
    private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager
    private lateinit var imageHalo: ImageHalo
    private lateinit var buttonColorChange: FloatingActionButton
    private var filters = arrayListOf<GPUImageFilter?>()
    private var adjusts = arrayListOf<Int>()
    private var tabPosition = 0
    private var filterAdjuster: GPUImageFilterTools.FilterAdjuster? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for(i in 0 .. 10) {

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
        tabLayout = view.findViewById(R.id.tabLayout)
        imageLiquify = view.findViewById(R.id.view_liquifyview)
        buttonColorChange = view.findViewById(R.id.buttonColorChange)
        buttonColorChange.setOnClickListener(this)

        imageManager = (activity?.application as ImageManager)

        gpuImage = GPUImage(context)

        buttonColorChange.visibility=View.GONE
        imageBG.visibility = View.VISIBLE
        seekBar.max = 100
        seekBar.progress = 50
        seekBar.visibility = View.GONE

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                if (tabPosition == 0) {
                    imageLiquify.brushsizechange(progress / 20)
                } else if (tabPosition != 8) {
                    adjusts[tabPosition] = progress
                    filterAdjuster = GPUImageFilterTools.FilterAdjuster(filters[tabPosition]!!)
                    filterAdjuster?.adjust(progress)
                    applyFilters(true)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (tabPosition == 8) {
                    if (seekBar != null) {
                        imageHalo.setWeight(seekBar.progress)
                    }
                    applyFilters(true)
                }
            }
        })
        tabLayout.addOnTabSelectedListener(tabListener)

    }


    fun refreshBackground(){
        setImageBitmap(imageBG,imageManager.backgroundFiltered)
    }

    public fun setImage() {
        setImageBitmap(imageFG,imageManager.personOriginal)
        setImageBitmap(imageBG,imageManager.backgroundFiltered)
        gpuImage.setImage(imageManager.personOriginal)
        imageLiquify.setup(
            30,
            50,
            imageManager.personOriginal,
            imageManager.backgroundOriginal
        )
        imageHalo = ImageHalo(imageManager.personOriginal,0, Color.WHITE)
        imageLiquify.visibility = View.VISIBLE
        seekBar.progress = 0
        seekBar.visibility = View.VISIBLE
    }

    public fun saveImage() {
        applyFilters(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_person, container, false)
    }

    private fun addFilter(f: GPUImageFilter) {
        val index = tabPosition
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
            when (tab?.position) {
                8 -> {
                    seekBar.visibility = View.GONE
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> {
                    imageLiquify.visibility = View.GONE
                    imageManager.personOriginal = imageLiquify.getLiquifiedImage(
                        imageManager.original.width,
                        imageManager.original.height
                    )
                }
                8 -> {
                }
            }
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            applyFilters(true)

            buttonColorChange.visibility=View.GONE
            seekBar.visibility = View.VISIBLE
            tabPosition = tab!!.position
            when (tab?.position) {
                0 -> {
                    imageLiquify.visibility = View.VISIBLE
                    seekBar.progress = 0
                    seekBar.visibility = View.VISIBLE
                }
                1 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.GAMMA
                    )
                )
                2 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.SATURATION
                    )
                )
                3 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.EXPOSURE
                    )
                )
                4 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.HIGHLIGHT_SHADOW
                    )
                )
                5 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.WHITE_BALANCE
                    )
                )
                6 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.HAZE
                    )
                )
                7 -> addFilter(
                    GPUImageFilterTools.createFilterForType(
                        context!!,
                        GPUImageFilterTools.FilterType.VIBRANCE
                    )
                )
                8 -> {
                    imageHalo.doHalo=true
                    buttonColorChange.visibility=View.VISIBLE
                }
                9->{
                    addFilter(
                        GPUImageFilterTools.createFilterForType(
                            context!!,
                            GPUImageFilterTools.FilterType.TOON
                        )
                    )
                }
            }
            if (tabPosition != 8)
                seekBar.progress = adjusts[tabPosition]
        }

    }

    fun applyFilters(toImageView:Boolean){
        var bitmap = Bitmap.createBitmap(imageManager.personOriginal)
        bitmap = gpuImage.getBitmapWithFiltersApplied(bitmap, filters)
        val haloTask = HaloTask()
        if(imageHalo.doHalo)haloTask.execute(bitmap)
        else{
            setImage(toImageView, bitmap)
        }

    }

    fun setImageBitmap(iv:ImageView,bitmap:Bitmap){
        Glide.with(this).load(bitmap).into(iv)
    }

    private fun setImage(toImageView: Boolean, bitmap:Bitmap){
        if(toImageView){
            if(toImageView)  setImageBitmap(imageFG,bitmap)
            else imageManager.personFiltered=bitmap
        }
    }

    inner class HaloTask: AsyncTask<Bitmap, Int, Bitmap>() {
        var toImageView=true

        override fun doInBackground(vararg p0: Bitmap): Bitmap {
            return imageHalo.run(p0[0])
        }

        override fun onPostExecute(result: Bitmap?) {
            setImage(toImageView, result!!)
            super.onPostExecute(result)
        }


    }

    override fun onClick(p0: View?) {
        val colorPicker = AmbilWarnaDialog(
            view?.context,
            Color.RED,
            true,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    return
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    imageHalo.color=color
                    applyFilters(true)
                }
            })
        colorPicker.show()    }

}
