package capstone.aiimageeditor.ui

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import capstone.aiimageeditor.R
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        try {
            val info = applicationContext.getPackageManager()
                .getPackageInfo(applicationContext.packageName, 0)
            val versionName = info.versionName
            text_version.setText(versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun OnBackgroundClick(v: View){
        finish()
    }
    fun OnFeedbackClick(v:View){

    }
    fun OnCopyrightClick(v:View){

    }
    fun OnReviewClick(v:View){

    }
}