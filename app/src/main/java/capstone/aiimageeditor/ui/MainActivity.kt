package capstone.aiimageeditor.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.viewpager.widget.ViewPager
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.adapter.TabPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import android.graphics.Bitmap as Bitmap

class MainActivity : AppCompatActivity() {


    private lateinit var imageNew:ImageView
    private lateinit var viewPager:ViewPager

/*    private lateinit var imageDefault:ImageView
    private lateinit var imageNow:ImageView*/
    private lateinit var buttonOriginal:ImageView
    private lateinit var fragmentMask: FragmentMask
    private lateinit var fragmentBackground: FragmentBackground
    private lateinit var fragmentPerson: FragmentPerson
    private lateinit var imageManager: ImageManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageManager = application as ImageManager

        imageNew = findViewById(R.id.image_new)
        buttonOriginal =findViewById(R.id.button_original)
        viewPager = findViewById(R.id.viewPager)

        buttonOriginal.setOnTouchListener(onOriginalButtonTouchListener)

        tabLayout.addOnTabSelectedListener(tabSelectedListener)
        initializeImage()
        fragmentBackground = FragmentBackground()
        fragmentMask = FragmentMask()
        fragmentPerson = FragmentPerson()

        val tabAdapter = TabPagerAdapter(supportFragmentManager,4)
        tabAdapter.addPage(fragmentMask,"마스크")
        tabAdapter.addPage(fragmentPerson,"인물")
        tabAdapter.addPage(fragmentBackground,"배경")
        tabAdapter.addPage(fragmentMask,"저장")

        viewPager.adapter=tabAdapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.addOnTabSelectedListener(tabSelectedListener)

    }
    val tabSelectedListener = object:TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when(tab!!.position){
                0->{

                }
                1->{ //배경
/*                    var bitmap = imageNow.drawable.toBitmap()
                    val outbit = PhotoProcessing.ApplyEnhance(bitmap,1,100)
                    imageNow.setImageBitmap(outbit)
                    imageDefault.setImageBitmap(outbit)
                }
                2-> { //전체
                    var bitmap = imageNow.drawable.toBitmap()
                    val outbit = PhotoProcessing.ApplyFilter(bitmap, 6, 100)
                    imageNow.setImageBitmap(outbit)
                    imageDefault.setImageBitmap(outbit)*/
                }
                3->{
                    val intent = Intent(this@MainActivity,
                        SaveActivity::class.java)
                    //(application as ImagePasser).image = imageNow.drawable.toBitmap()
                    startActivity(intent)
                }
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }
    }

    fun initializeImage(){
        imageNew.setImageBitmap(imageManager.original)
/*        imageDefault.setImageURI(imageUri)
        imageNow.setImageURI(imageUri)*/
        //TODO stack 초기화 시켜주기
    }

    val onOriginalButtonTouchListener = object:View.OnTouchListener{
        override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
            if(p1!=null)
            {
                when(p1.action){
                    //TODO 원본 보여주
                }
            }
            return true
        }
    }

    fun onBackButtonClick(v: View){
        //TODO stack에 무언가 쌓였으면 체크 후 뒤로가기
        finish()
    }
    fun onUndoButtonClick(v:View){
        //TODO stack 구현 후 undo 구현
/*        var original = Mat()
        Utils.bitmapToMat(imageNow.drawable.toBitmap(),original)
        runMaskCorrector(original.nativeObjAddr,original.nativeObjAddr)
        var bmp = Bitmap.createBitmap(imageNow.drawable.toBitmap())
        Utils.matToBitmap(original,bmp)
        imageDefault.setImageBitmap(bmp)*/
    }
    fun onRedoButtonClick(v:View){
        //TODO stack 구현 후 redo 구현
    }
    fun onNewButtonClick(v:View){
        //TODO stack에 무언가 쌓였으면 체크 후 새로 하기
        val intent = Intent(Intent.ACTION_PICK)
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
        startActivityForResult(intent,
            StartActivity.PICK_FROM_ALBUM
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var masked:Long=0
        val originalImage = imageNew.drawable.toBitmap()
        var original = Mat()
        var mask = Mat()
        if(requestCode== StartActivity.PICK_FROM_ALBUM && data!=null){
            val imageUri = data.data as Uri
            imageManager.loadMask(imageUri)
            val originalMask = imageManager.mask

            Utils.bitmapToMat(originalImage,original)
            Utils.bitmapToMat(originalMask,mask)
            //startInpaint(original.nativeObjAddr,mask.nativeObjAddr)
            runMaskCorrector(original.nativeObjAddr,mask.nativeObjAddr)
            val bitmap = Bitmap.createBitmap(originalMask)
            Utils.matToBitmap(mask, imageManager.mask)
            fragmentMask.setImage()//imageNew.drawable.toBitmap()//imageManager.getImageFromUri(imageUri)

        }

        super.onActivityResult(requestCode, resultCode, data)
    }
    external fun startInpaint(image: Long, mask: Long)
    external fun runMaskCorrector(imagePtr:Long, maskPtr:Long)


    override fun onResume() {
        super.onResume()
        tabLayout.getTabAt(0)?.select()

    }
}