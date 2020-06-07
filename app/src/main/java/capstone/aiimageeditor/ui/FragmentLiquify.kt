package capstone.aiimageeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import com.google.android.material.tabs.TabLayout
import capstone.aiimageeditor.customviews.LiquifyView
import kotlinx.android.synthetic.main.fragment_liquify.*
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*


class FragmentLiquify : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var imageManager: ImageManager



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { //oncreateview다음으로 실행
        super.onViewCreated(view, savedInstanceState)
        tabLayout = view.findViewById(R.id.tabLayout)
        tabLayout.addOnTabSelectedListener(tabListener)
        imageManager = (activity?.application as ImageManager)
        //Log.i("##fragmentliquify", "초기화완료")

        view_liquifyview.setup(
            30,
            50,
            imageManager.personOriginal,
            imageManager.backgroundOriginal
        )


    }


    val tabListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {}
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabSelected(tab: TabLayout.Tab?) {
            when (tab?.position) {
                0 -> view_liquifyview.brushsizechange(0)
                1 -> view_liquifyview.brushsizechange(1)
                2 -> view_liquifyview.brushsizechange(2)
                3 -> view_liquifyview.brushsizechange(3)
                4 -> view_liquifyview.brushsizechange(4)
            }
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liquify, container, false)
    }
}
