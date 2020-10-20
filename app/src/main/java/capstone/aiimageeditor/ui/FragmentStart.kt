package capstone.aiimageeditor.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import capstone.aiimageeditor.ImageManager
import capstone.aiimageeditor.R
import capstone.aiimageeditor.adapter.AdapterImageList
import capstone.aiimageeditor.databinding.FragmentStartBinding
import capstone.aiimageeditor.symmenticsegmentation.ImageSegmentationModelExecutor
import capstone.aiimageeditor.symmenticsegmentation.MLExecutionViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.*
import java.util.concurrent.Executors

class FragmentStart:BaseKotlinFragment<FragmentStartBinding>(){
    override val layoutResourceId: Int
        get() = R.layout.fragment_start

    companion object {
        // Used to load the 'native-lib' library on application startup.
        const val PICK_FROM_ALBUM = 1
        private const val PREF_STRING_SET_KEY = "URI"

        init {
            System.loadLibrary("native-lib")
        }
    }

    private lateinit var viewModel: MLExecutionViewModel
    private lateinit var imageSegmentationModel: ImageSegmentationModelExecutor
    private var useGPU = false
    private val inferenceThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    var images = ArrayList<String>()
    private lateinit var adapter: AdapterImageList

    override fun initStartView() {
        tedPermission()
        if (getSavedStringSets() != null) images = getSavedStringSets()!!

/*        if (intent?.action == Intent.ACTION_SEND) {
            if (intent?.type?.startsWith("image/") == true) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri).let {
                    val photoUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri
                    for (i in images.indices) {
                        if (images[i] == photoUri.toString()) {
                            images.removeAt(i)
                            break
                        }
                    }
                    images.add(0, photoUri.toString())
                    if (images.size > 10) images.removeAt(images.lastIndex)
                    gotoNextActivity(photoUri)
                }
            }
        }    */
    }

    override fun initDataBinding() {
        adapter = AdapterImageList(requireContext(), images)
        binding.recyclerView.adapter = adapter


        val linearLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.recyclerView.layoutManager = linearLayoutManager
        binding.recyclerView.setHasFixedSize(true)

    }

    override fun initAfterBinding() {
        adapter.setOnItemClickListener(object : AdapterImageList.OnItemClickListener {
            override fun onClick(position: Int) {
                if (position == 0) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = MediaStore.Images.Media.CONTENT_TYPE
                    startActivityForResult(intent, PICK_FROM_ALBUM)
                } else {
                    val str = images[position - 1]
                    images.remove(str)
                    images.add(0, str)
                    gotoNextActivity(Uri.parse(str))
                }

            }
        })

        viewModel = ViewModelProviders.of(this).get(MLExecutionViewModel::class.java)
        viewModel.resultingBitmap.observe(this, Observer { resultImage ->
            if (resultImage != null) {
               (requireActivity().application as ImageManager).mask = resultImage.bitmapMaskOnly
                (requireActivity().application as ImageManager).runMaskCorrection()
                findNavController().navigate(FragmentStartDirections.actionFragmentStartToFragmentMain())
            }
        })
    }

    override fun reLoadUI() {
    }


    private fun gotoNextActivity(uri: Uri) {
        if ((requireActivity().application as ImageManager).loadOriginal(uri)) {
            saveStringSet(images)
            imageSegmentationModel = ImageSegmentationModelExecutor(requireContext(), useGPU)
            viewModel.onApplyModel(imageSegmentationModel, inferenceThread, (requireActivity().application as ImageManager).original)
        } else {
            images.remove(uri.toString())
            Toast.makeText(requireContext(), resources.getString(R.string.error_load_image), Toast.LENGTH_LONG)
            adapter.notifyDataSetChanged()
        }

    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    private fun tedPermission() {
        val permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {

            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                Toast.makeText(requireContext(), resources.getString(R.string.error_permission), Toast.LENGTH_LONG)

            }

        }

        TedPermission.with(requireContext())
            .setPermissionListener(permissionListener)
            .setRationaleMessage(R.string.permission_desc)
            .setDeniedMessage(R.string.permission_rej)
            .setPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.INTERNET
            )
            .check()


    }

    private fun saveStringSet(mList: ArrayList<String>) {
        val sp: SharedPreferences = requireActivity().getSharedPreferences("uri", Context.MODE_PRIVATE)//PreferenceManager.getDefaultSharedPreferences(context)
        val editor: SharedPreferences.Editor = sp.edit()
        editor.putString(PREF_STRING_SET_KEY, Gson().toJson(mList))
        editor.apply()
    }

    private fun getSavedStringSets(): ArrayList<String>? {
        val sp = requireActivity().getSharedPreferences("uri", Context.MODE_PRIVATE)
        return Gson().fromJson(
            sp.getString(PREF_STRING_SET_KEY, null),
            TypeToken.getParameterized(ArrayList::class.java, String::class.java).type
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_FROM_ALBUM && data != null) {

            val photoUri = data.data as Uri
            for (i in images.indices) {
                if (images[i] == photoUri.toString()) {
                    images.removeAt(i)
                    break
                }
            }
            images.add(0, photoUri.toString())
            if (images.size > 10) images.removeAt(images.lastIndex)
            gotoNextActivity(photoUri)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}