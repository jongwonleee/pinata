package capstone.aiimageeditor.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

data class Page(var title: String, val fragment: Fragment)


class TabPagerAdapter(fm: FragmentManager, behavior: Int) : FragmentStatePagerAdapter(fm, behavior) {
    private var pages: ArrayList<Page> = arrayListOf()


    override fun getItem(position: Int): Fragment {
        return pages[position].fragment
    }

    fun addPage(fragment: Fragment, title: String) {
        pages.add(Page(title, fragment))
    }

//    fun setPageString(index:Int,title:String){
//        pages[index].title=title
//        notifyDataSetChanged()
//    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pages[position].title
    }


    override fun getCount(): Int {
        return pages.size
    }
}
