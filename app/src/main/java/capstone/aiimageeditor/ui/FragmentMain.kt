package capstone.aiimageeditor.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.adapter.TabPagerAdapter
import capstone.aiimageeditor.databinding.FragmentMainBinding
import capstone.aiimageeditor.symmenticsegmentation.MaskSeparator
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout

class FragmentMain: BaseKotlinFragment<FragmentMainBinding>() {
    override val layoutResourceId: Int
        get() = R.layout.fragment_main

    private val fragmentMask = FragmentMask()
    private val fragmentBackground= FragmentBackground()
    private val fragmentPerson = FragmentPerson()
    private lateinit var imageManager: ImageManager
    private lateinit var maskSeparator: MaskSeparator
    private var saveEnabled = false

    companion object {
        private val titles = listOf("마스크", "인물", "배경", "저장")
        private val iconsOff = listOf(R.drawable.ic_person_off, R.drawable.ic_user_off, R.drawable.ic_background_off, R.drawable.ic_done_off)
        private val iconsOn = listOf(R.drawable.ic_person_on, R.drawable.ic_user_on, R.drawable.ic_background_on, R.drawable.ic_done_on)
        private val ON_SAVE_ACTIVITY_RESULT = 0
        private var backKeyPressedTime: Long = 0
        private var tabIndexLast = 0
    }

    override fun initStartView() {
        imageManager = requireActivity().application as ImageManager
        imageManager.initialize()

        maskSeparator = MaskSeparator()
        initializeImage()

    }

    override fun initDataBinding() {
        val fragmentEmpty = FragmentMask()
        val tabAdapter = TabPagerAdapter(requireActivity().supportFragmentManager, 4) //behavior 4 -> 5
        if(!fragmentMask.isAdded) tabAdapter.addPage(fragmentMask, "")
        if(!fragmentPerson.isAdded)tabAdapter.addPage(fragmentPerson, "")
        if(!fragmentBackground.isAdded)tabAdapter.addPage(fragmentBackground, "")
        if(!fragmentEmpty.isAdded) tabAdapter.addPage(fragmentEmpty, "")

        setTabView(0, true)
        setTabView(1, false)
        setTabView(2, false)
        setTabView(3, false)

        binding.viewPager.adapter = tabAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.tabLayout.addOnTabSelectedListener(tabSelectedListener)
    }

    override fun initAfterBinding() {
        binding.buttonOriginal.setOnTouchListener(onOriginalButtonTouchListener)
        binding.buttonBack.setOnClickListener {
            //TODO stack에 무언가 쌓였으면 체크 후 뒤로가기
            imageManager.InpaintTask().cancel(true)
            findNavController().popBackStack()
        }
        binding.buttonRedo.setOnClickListener {
            //TODO stack 구현 후 redo 구현
        }
        binding.buttonSetting.setOnClickListener{
            /*        val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)*/
        }
        binding.buttonUndo.setOnClickListener{

        }
        binding.imageNew.setOnClickListener{
            //TODO stack에 무언가 쌓였으면 체크 후 새로 하기
            val intent = Intent(Intent.ACTION_PICK)
            intent.setType(MediaStore.Images.Media.CONTENT_TYPE)
            startActivityForResult(
            intent,
            FragmentStart.PICK_FROM_ALBUM
            )
        }
        imageManager.setOnFinishInpaint(object : ImageManager.OnFinishInpaint {
            override fun onFinishInpaint() {
                fragmentBackground.setImage()
                fragmentPerson.refreshBackground()
                saveEnabled = true
                (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(3).isEnabled = true
                Toast.makeText(requireActivity(), "이제 저장하실 수 있습니다", Toast.LENGTH_LONG).show()
                setTabView(3, false)

            }
        })
        (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(3).isEnabled = false
    }

    override fun reLoadUI() {
    }

    fun setImageBitmap(iv: ImageView, bitmap: Bitmap) {
        Glide.with(this).load(bitmap).into(iv)
    }

    fun initializeImage() {
        setImageBitmap(binding.imageNew, imageManager.original)
        setImageBitmap(binding.imageOriginal, imageManager.original)
        //TODO stack 초기화 시켜주기
    }

    private val onOriginalButtonTouchListener = object : View.OnTouchListener {
        override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
            if (p1 != null) {
                when (p1.action) {
                    MotionEvent.ACTION_DOWN -> binding.imageOriginal.visibility = View.VISIBLE
                    MotionEvent.ACTION_UP -> binding.imageOriginal.visibility = View.GONE
                }
            }
            return true
        }

    }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            setTabView(tab!!.position, true)
            when (tab.position) {
                0 -> {
                    imageManager.InpaintTask().cancel(true)
                    fragmentMask.setImage(requireContext().applicationContext)
                }
                1 -> {
                    fragmentPerson.setImage()
                }
                2 -> {
                    fragmentBackground.setImage()
                }
                3 -> {
                    if (saveEnabled) {
//                        val intent = Intent(requireContext(), FragmentSave::class.java)
//                        startActivityForResult(intent, ON_SAVE_ACTIVITY_RESULT)
                          findNavController().navigate(FragmentMainDirections.actionFragmentMainToFragmentSave())
                    }

                }
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {
            setTabView(tab!!.position, true)
            when (tab.position) {
                0 -> {
                    imageManager.InpaintTask().cancel(true)
                    fragmentMask.setImage(requireContext().applicationContext)
                }
                1 -> {
                    fragmentPerson.setImage()
                }
                2 -> {
                    fragmentBackground.setImage()
                }
            }
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            setTabView(tab!!.position, false)
            tabIndexLast = tab.position
            when (tab.position) {
                0 -> {
                    fragmentMask.deleteView()
                    imageManager.personOriginal = maskSeparator.applyWithMask(imageManager.original, imageManager.mask)
                    imageManager.personFiltered = Bitmap.createBitmap(imageManager.personOriginal)
                    imageManager.startInpaint()
                    saveEnabled = false
                    (binding.tabLayout.getChildAt(0) as ViewGroup).getChildAt(3).isEnabled = false
                    setTabView(3, false)
                }
                1 -> {
                    fragmentPerson.saveImage()
                }
                2 -> {
                    fragmentBackground.saveImage()
                }
            }
        }
    }

//    override fun onBackPressed() {
//        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
//            backKeyPressedTime = System.currentTimeMillis()
//            Toast.makeText(requireContext(), "뒤로 갈 시 현재까지의 결과물은 저장되지 않습니다.\n한번 더 누를 시 시작 액티비티로 이동합니다", Toast.LENGTH_SHORT).show()
//            return
//        }
//        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
//            imageManager.InpaintTask().cancel(true)
//            //finish()
//        }
//
//        super.onBackPressed()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FragmentStart.PICK_FROM_ALBUM && data != null) {
        } else if (requestCode == ON_SAVE_ACTIVITY_RESULT) {
            binding.tabLayout.getTabAt(tabIndexLast)?.select()
            setTabView(tabIndexLast, true)
            setTabView(3, false)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun setTabView(pos: Int, selected: Boolean) {
        Log.i("changing Tab", "$pos, $selected ${if (selected) iconsOn[pos] else iconsOff[pos]}")
        val view = layoutInflater.inflate(R.layout.tab_view_main, null)
        val title = view.findViewById(R.id.title) as TextView
        val image = view.findViewById(R.id.icon) as ImageView
        if (pos == 3 && !saveEnabled) {
            title.text = titles[pos]
            title.setTextColor(Color.parseColor("#DDDDDD"))
            image.setImageResource(R.drawable.ic_done_disabled)
        } else {
            title.text = titles[pos]
            title.setTextColor(Color.parseColor(if (selected) "#ff6f69" else "#909090"))
            image.setImageResource(if (selected) iconsOn[pos] else iconsOff[pos])
        }
        binding.tabLayout.getTabAt(pos)?.customView = null
        binding.tabLayout.getTabAt(pos)?.customView = view
        binding.tabLayout.refreshDrawableState()
    }
}