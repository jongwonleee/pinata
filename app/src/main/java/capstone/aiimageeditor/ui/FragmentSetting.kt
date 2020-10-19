package capstone.aiimageeditor.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import capstone.aiimageeditor.R
import capstone.aiimageeditor.databinding.FragmentSettingBinding

class FragmentSetting :BaseKotlinFragment<FragmentSettingBinding>(){
    override val layoutResourceId: Int
        get() = R.layout.fragment_setting

    companion object{
        val packageName = "capstone.aiimageeditor"
    }

    override fun initStartView() {
        try {
            val info = requireActivity().applicationContext.packageManager
                .getPackageInfo(requireActivity().applicationContext.packageName, 0)
            binding.textVersion.text = info.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
        binding.background.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonFeedback.setOnClickListener{
            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf("jong12ems@google.com"))
            email.putExtra(Intent.EXTRA_SUBJECT, "앱 Pinata 관련 피드백")
            email.type = "message/rfc822"
            startActivity(Intent.createChooser(email, "Choose an Email client :"))
        }
        binding.buttonCopyright.setOnClickListener {
            findNavController().navigate(FragmentSettingDirections.actionFragmentSettingToFragmentCopyright())
        }
        binding.buttonPersonalData.setOnClickListener {
            findNavController().navigate(FragmentSettingDirections.actionFragmentSettingToFragmentRegulation())
        }
        binding.buttonReview.setOnClickListener {
            val uri: Uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")))
            }
        }
    }

    override fun reLoadUI() {
    }

}
/*

class SettingActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    fun OnRegulationClick(v: View) {
        val intent = Intent(this, RegulationActivity::class.java)
        startActivity(intent)
    }

    fun OnFeedbackClick(v: View) {

    }

    fun OnCopyrightClick(v: View) {
        val intent = Intent(this, CopyrightActivity::class.java)
        startActivity(intent)
    }

    fun OnReviewClick(v: View) {

    }
}
 */