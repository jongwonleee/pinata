package capstone.aiimageeditor.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.viewpager.widget.ViewPager
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.adapter.TabPagerAdapter
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
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
                    val intent = Intent(this@MainActivity, SaveActivity::class.java)
                    startActivity(intent)
                }
                4-> { //for liquify
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
                    imageManager.setOnFinishInpaint(object:ImageManager.OnFinishInpaint{
                        override fun onFinishInpaint() {
                            fragmentBackground.setImage()
                            fragmentPerson.refreshBackground()
                        }
                    })
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

    fun initializeImage() {
        imageNew.setImageBitmap(imageManager.original)
        imageOriginal.setImageBitmap(imageManager.original)
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
        finish()
    }

    fun onUndoButtonClick(v: View) {
        //TODO stack 구현 후 undo 구현
/*        var original = Mat()
        Utils.bitmapToMat(imageNow.drawable.toBitmap(),original)
        runMaskCorrector(original.nativeObjAddr,original.nativeObjAddr)
        var bmp = Bitmap.createBitmap(imageNow.drawable.toBitmap())
        Utils.matToBitmap(original,bmp)
        imageDefault.setImageBitmap(bmp)*/
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

    external fun runMaskCorrector(imagePtr: Long, maskPtr: Long)

    override fun onResume() {
        tabLayout.getTabAt(0)?.select()
        super.onResume()
    }
}