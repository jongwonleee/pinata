package capstone.aiimageeditor.ui

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.adapter.TabPagerAdapter
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var imageNew: ImageView
    private lateinit var viewPager: ViewPager


    private lateinit var buttonOriginal: ImageView
    private lateinit var imageOriginal:ImageView
    private lateinit var fragmentMask: FragmentMask
    private lateinit var fragmentBackground: FragmentBackground
    private lateinit var fragmentPerson: FragmentPerson
    private lateinit var imageManager: ImageManager
    private lateinit var maskSeparator: MaskSeparator
    private var saveEnabled=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageManager = application as ImageManager

        imageNew = findViewById(R.id.image_new)
        buttonOriginal = findViewById(R.id.button_original)
        viewPager = findViewById(R.id.viewPager)
        imageOriginal=findViewById(R.id.image_original)
        maskSeparator = MaskSeparator()

        buttonOriginal.setOnTouchListener(onOriginalButtonTouchListener)

        initializeImage()
        fragmentBackground = FragmentBackground()
        fragmentMask = FragmentMask()
        fragmentPerson = FragmentPerson()
        val fragmentEmpty=FragmentMask()
        val tabAdapter = TabPagerAdapter(supportFragmentManager, 4) //behavior 4 -> 5
        tabAdapter.addPage(fragmentMask, "마스크")
        tabAdapter.addPage(fragmentPerson, "인물")
        tabAdapter.addPage(fragmentBackground, "배경")
        tabAdapter.addPage(fragmentEmpty, "저장")

        viewPager.adapter = tabAdapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(tabSelectedListener)
        imageManager.setOnFinishInpaint(object:ImageManager.OnFinishInpaint{
            override fun onFinishInpaint() {
                fragmentBackground.setImage()
                fragmentPerson.refreshBackground()
                saveEnabled=true
                (tabLayout.getChildAt(0) as ViewGroup).getChildAt(3).isEnabled=true
                Toast.makeText(this@MainActivity,"이제 저장하실 수 있습니다",Toast.LENGTH_LONG)

            }
        })
        (tabLayout.getChildAt(0) as ViewGroup).getChildAt(3).isEnabled=false

    }

    val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab!!.position) {
                0 -> {
                    imageManager.InpaintTask().cancel(true)
                    fragmentMask.setImage(this@MainActivity.applicationContext)
                }
                1 -> {
                    fragmentPerson.setImage()
                }
                2 -> {
                    fragmentBackground.setImage()
                }
                3 -> {
                    if(saveEnabled){
                        val intent = Intent(this@MainActivity, SaveActivity::class.java)
                        startActivity(intent)
                    }

                }
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            when (tab!!.position) {
                0 -> {
                    fragmentMask.deleteView()
                    imageManager.personOriginal = maskSeparator.applyWithMask(imageManager.original, imageManager.mask)
                    imageManager.personFiltered = Bitmap.createBitmap(imageManager.personOriginal)
                    imageManager.startInpaint()
                    saveEnabled=false
                    (tabLayout.getChildAt(0) as ViewGroup).getChildAt(3).isEnabled=false
                }
                1->{
                    fragmentPerson.saveImage()
                }
                2->{
                    fragmentBackground.saveImage()
                }
            }
        }
    }
    fun setImageBitmap(iv:ImageView,bitmap:Bitmap){
        Glide.with(this).load(bitmap).into(iv)
    }
    fun initializeImage() {
        setImageBitmap(imageNew,imageManager.original)
        setImageBitmap(imageOriginal,imageManager.original)
        //TODO stack 초기화 시켜주기
    }

    val onOriginalButtonTouchListener = object : View.OnTouchListener {
        override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
            if (p1 != null) {
                when (p1.action) {
                    MotionEvent.ACTION_DOWN-> imageOriginal.visibility= View.VISIBLE
                    MotionEvent.ACTION_UP -> imageOriginal.visibility= View.GONE
                }
            }
            return true
        }

    }

    fun onBackButtonClick(v: View) {
        //TODO stack에 무언가 쌓였으면 체크 후 뒤로가기
        imageManager.InpaintTask().cancel(true)
        finish()
    }

    fun onSettingButtonClick(v:View){
        val intent = Intent(this, SettingActivity::class.java)
        startActivity(intent)
    }

    fun onUndoButtonClick(v: View) {
        //TODO stack 구현 후 undo 구현
/*        var original = Mat()
        Utils.bitmapToMat(imageNow.drawable.toBitmap(),original)
        runMaskCorrector(original.nativeObjAddr,original.nativeObjAddr)
        var bmp = Bitmap.createBitmap(imageNow.drawable.toBitmap())
        Utils.matToBitmap(original,bmp)
        imageDefaultisUpitmap(bmp)*/
    }

    fun onRedoButtonClick(v: View) {
        //TODO stack 구현 후 redo 구현
    }

    fun onNewButtonClick(v: View) {
        //TODO stack에 무언가 쌓였으면 체크 후 새로 하기
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
        startActivityForResult(
            intent,
            StartActivity.PICK_FROM_ALBUM
        )
    }

    override fun onBackPressed() {
        imageManager.InpaintTask().cancel(true)
        finish()
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == StartActivity.PICK_FROM_ALBUM && data != null) {
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        tabLayout.getTabAt(0)?.select()
        super.onResume()
    }
}