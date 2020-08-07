package capstone.aiimageeditor.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import capstone.aiimageeditor.databinding.ActivitySettingBinding

class SettingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            val info = applicationContext.packageManager
                .getPackageInfo(applicationContext.packageName, 0)
            binding.textVersion.text = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun OnBackgroundClick(v: View) {
        finish()
    }

    fun OnRegulationClick(v: View) {
        val intent = Intent(this, RegulationActivity::class.java)
        startActivity(intent)
    }

    fun OnFeedbackClick(v: View) {
        val email = Intent(Intent.ACTION_SEND)
        email.putExtra(Intent.EXTRA_EMAIL, arrayOf("jong12ems@google.com"))
        email.putExtra(Intent.EXTRA_SUBJECT, "앱 Pinata 관련 피드백")
        email.type = "message/rfc822"
        startActivity(Intent.createChooser(email, "Choose an Email client :"))
    }

    fun OnCopyrightClick(v: View) {
        val intent = Intent(this, CopyrightActivity::class.java)
        startActivity(intent)
    }

    fun OnReviewClick(v: View) {

    }
}