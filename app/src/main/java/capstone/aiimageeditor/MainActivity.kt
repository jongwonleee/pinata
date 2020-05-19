package capstone.aiimageeditor

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.opencv.android.Utils
import org.opencv.core.Mat
import android.graphics.Bitmap as Bitmap

class MainActivity : AppCompatActivity() {

    private var imageUri:Uri = Uri.EMPTY
    private var maskUri:Uri = Uri.EMPTY

    private lateinit var imageNew:ImageView
    private lateinit var imageDefault:ImageView
    private lateinit var imageNow:ImageView
    private lateinit var buttonOriginal:ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageDefault = findViewById(R.id.image_default)
        imageNow = findViewById(R.id.image_now)
        imageNew = findViewById(R.id.image_new)
        buttonOriginal =findViewById(R.id.button_original)

        buttonOriginal.setOnTouchListener(onOriginalButtonTouchListener)

        imageUri = intent.getParcelableExtra("photo") as Uri
        tabLayout.addOnTabSelectedListener(tabSelectedListener)
        initializeImage()


    }
    val tabSelectedListener = object:TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when(tab!!.position){
                0->{

                }
                1->{ //배경
                    var bitmap = imageNow.drawable.toBitmap()
                    val outbit = PhotoProcessing.ApplyEnhance(bitmap,1,100)
                    imageNow.setImageBitmap(outbit)
                    imageDefault.setImageBitmap(outbit)
                }
                2-> { //전체
                    var bitmap = imageNow.drawable.toBitmap()
                    val outbit = PhotoProcessing.ApplyFilter(bitmap, 6, 100)
                    imageNow.setImageBitmap(outbit)
                    imageDefault.setImageBitmap(outbit)
                }
                3->{
                    val intent = Intent(this@MainActivity,SaveActivity::class.java)
                    (application as ImagePasser).image = imageNow.drawable.toBitmap()
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
        imageNew.setImageURI(imageUri)
        imageDefault.setImageURI(imageUri)
        imageNow.setImageURI(imageUri)
        //TODO stack 초기화 시켜주기
    }

    val onOriginalButtonTouchListener = object:View.OnTouchListener{
        override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
            if(p1!=null)
            {
                when(p1.action){
                    MotionEvent.ACTION_DOWN ->{
                        Log.i("!!","1")
                        imageDefault.visibility=View.VISIBLE
                    }
                    MotionEvent.ACTION_UP->{
                        Log.i("!!","2")
                        imageDefault.visibility=View.GONE
                    }
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
        startActivityForResult(intent, StartActivity.PICK_FROM_ALBUM)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var masked:Long=0
        var original = Mat()
        var mask = Mat()
        if(requestCode== StartActivity.PICK_FROM_ALBUM && data!=null){
          /* imageUri = data.data as Uri
            initializeImage()*/
            maskUri = data.data as Uri
            imageNew.setImageURI(maskUri)

            Utils.bitmapToMat(imageNow.drawable.toBitmap(),original)
            Utils.bitmapToMat(imageNew.drawable.toBitmap(),mask)
            startInpaint(original.nativeObjAddr,mask.nativeObjAddr)
            //runMaskCorrector(original.nativeObjAddr,mask.nativeObjAddr)
        }
        var bitmap = Bitmap.createBitmap(imageNow.drawable.toBitmap())
        Utils.matToBitmap(original,bitmap)
        imageDefault.setImageBitmap(bitmap)
        super.onActivityResult(requestCode, resultCode, data)
    }
    external fun startInpaint(image: Long, mask: Long)
    external fun runMaskCorrector(imagePtr:Long, maskPtr:Long)
}