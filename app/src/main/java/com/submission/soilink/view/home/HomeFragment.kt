package com.submission.soilink.view.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.submission.soilink.util.EXTRA_IMAGE_URI
import com.submission.soilink.util.accountName
import com.submission.soilink.util.reduceFileImage
import com.submission.soilink.util.showToast
import com.submission.soilink.util.uriToFile
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.about.AboutActivity
import com.submission.soilink.view.profile.ProfileActivity
import com.submission.soilink.view.result.ResultActivity
import com.submission.soilink.view.soillist.SoilListActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            showToast(requireContext(), getString(R.string.select_image))
            currentImageUri = uri
            processImage()
        } else {
            showToast(requireContext(), getString(R.string.failed_choose_image))
        }
    }

    private fun processImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, requireContext()).reduceFileImage()

            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.check_soil_type))
                setCancelable(false)
                setNegativeButton(getString(R.string.btn_yes)) { _, _ ->
                    showToast(requireContext(), getString(R.string.start_check))

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
                setPositiveButton(getString(R.string.btn_no)) { _, _ ->
                    showToast(requireContext(), getString(R.string.not_processing))
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