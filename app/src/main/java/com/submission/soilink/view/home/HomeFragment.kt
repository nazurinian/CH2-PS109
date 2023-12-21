package com.submission.soilink.view.home

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore
import com.submission.soilink.databinding.FragmentHomeBinding
import com.submission.soilink.util.accountName
import com.submission.soilink.util.getCurrentDate
import com.submission.soilink.util.reduceFileImage
import com.submission.soilink.util.showToast
import com.submission.soilink.util.uriToFile
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.about.AboutActivity
import com.submission.soilink.view.profile.ProfileActivity
import com.submission.soilink.view.result.ResultActivity
import com.submission.soilink.view.result.ResultActivity.Companion.EXTRA_IMAGE_URI
import com.submission.soilink.view.soillist.SoilListActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.regex.Matcher
import java.util.regex.Pattern


class HomeFragment : Fragment() {

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = UserPreference.getInstance(requireActivity().dataStore)
        val user = runBlocking { pref.getSession().first() }

        setupAction(user.name)
    }

    private fun setupAction(userName: String) {
        val toolbar = binding.topAppBar
        toolbar.title = activity?.getString(R.string.user_login, userName)
        accountName = userName

        binding.btnProfile.setOnClickListener {
            val intent = Intent(activity, ProfileActivity::class.java)
            intent.putExtra(ProfileActivity.DATA_PROFILE, userName)
            startActivity(intent)
        }

        binding.btnCheckSoil.setOnClickListener {
            startGallery()
        }

        binding.btnSoilList.setOnClickListener {
            val intent = Intent(activity, SoilListActivity::class.java)
            startActivity(intent)
        }

        binding.btnAboutSoilInk.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            startActivity(intent)
            showToast(requireContext(), getCurrentDate())
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            showToast(requireContext(), "Telah memilih gambar")
            val fileName = getRealPathFromDocumentUri(requireContext(), uri)
            val fileNames = fileName.let { Uri.parse(it) }
            currentImageUri = fileNames!!
            processImage()
        } else {
            showToast(requireContext(), "Tidak ada gambar yang dipilih")
        }
    }

    private fun getRealPathFromDocumentUri(context: Context, uri: Uri): String {
        var filePath = ""
        val p: Pattern = Pattern.compile("(\\d+)$")
        val m: Matcher = p.matcher(uri.toString())
        if (!m.find()) {
            return filePath
        }
        val imgId = m.group()
        val column = arrayOf(MediaStore.Images.Media.DATA)
        val sel = MediaStore.Images.Media._ID + "=?"
        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            column, sel, arrayOf(imgId), null
        )
        val columnIndex = cursor!!.getColumnIndex(column[0])
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex)
        }
        cursor.close()
        return filePath
    }

    private fun processImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, requireContext()).reduceFileImage()

            AlertDialog.Builder(requireContext()).apply {
                setTitle("Cek tipe tanah?")
                setCancelable(false)
                setNegativeButton("Yes") { _, _ ->
                    showToast(requireContext(), "Memulai proses cek tipe tanah")
                    //logika upload data ke internet sekaligus proses bar on loading, success, error
                    viewModel.uploadPicture(imageFile).observe(requireActivity()) { result ->
                        if (result != null) {
                            when (result) {
                                is ResultState.Loading -> {
                                    showLoading(true)
                                }

                                is ResultState.Success -> {
                                    showToast(context, getString(R.string.post_successfull))
                                    showLoading(false)
                                    val intent = Intent(context, ResultActivity::class.java)
                                    intent.putExtra(EXTRA_IMAGE_URI, uri.toString())
                                    startActivity(intent)
                                }

                                is ResultState.Error -> {
                                    showToast(requireContext(), result.error)
                                    showLoading(false)
                                }
                            }
                        }
                    }
                }
                setPositiveButton("No") { _, _ ->
                    showToast(requireContext(), "Tidak jadi memprosess")
                }
                create()
                show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val USER_NAME = "USER_NAME"
    }

}