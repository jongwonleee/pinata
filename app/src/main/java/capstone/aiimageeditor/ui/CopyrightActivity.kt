package capstone.aiimageeditor.ui

import android.app.Activity
import android.content.Intent
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
        val email = Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf("jong12ems@google.com"))
        email.putExtra(Intent.EXTRA_SUBJECT, "앱 Pinata 관련 피드백")
        email.setType("message/rfc822")
        startActivity(Intent.createChooser(email, "Choose an Email client :"))
    }
    fun OnCopyrightClick(v:View){

    }
    fun OnReviewClick(v:View){

    }
}