package capstone.aiimageeditor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment

/**
 * Databinding을 진행할 fragment들의 기본 형식
 * T를 통해 그 fragment의 DataBinding 객체를 가져오고
 * 이를 통해 fragment binding을 진행한다
 */
abstract class BaseKotlinFragment<T : ViewDataBinding> : Fragment() {

    lateinit var binding: T

    /**
     * setContentView로 호출할 Layout의 리소스 Id.
     * ex) R.layout.activity_sbs_main
     */
    abstract val layoutResourceId: Int

    /**
     * 레이아웃을 띄운 직후 호출.
     * 뷰나 액티비티의 속성 등을 초기화.
     * ex) 리사이클러뷰, 툴바, 드로어뷰..
     */
    abstract fun initStartView()

    /**
     * 두번째로 호출.
     * 데이터 바인딩 및 rxjava 설정.
     * ex) rxjava observe, databinding observe..
     */
    abstract fun initDataBinding()

    /**
     * 바인딩 이후에 할 일을 여기에 구현.
     * 그 외에 설정할 것이 있으면 이곳에서 설정.
     * 클릭 리스너도 이곳에서 설정.
     */
    abstract fun initAfterBinding()

    /**
     * 초기설정 이외, onResume 됬을때의 View Update 를 위한 함수
     * Observing 형식의 데이터만 아닌 경우에만 해당
     */
    abstract fun reLoadUI()

    private var isSetBackButtonValid = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(!(::binding.isInitialized)) {
            binding = DataBindingUtil.inflate(inflater, layoutResourceId, container, false)
            initStartView()
            initDataBinding()
            initAfterBinding()
        }
        reLoadUI()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}