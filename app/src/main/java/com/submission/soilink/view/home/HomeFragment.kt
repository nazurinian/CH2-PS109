package com.submission.soilink.view.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore
import com.submission.soilink.databinding.FragmentHomeBinding
import com.submission.soilink.util.EXTRA_IMAGE_URI
import com.submission.soilink.util.NetworkCheck
import com.submission.soilink.util.accountName
import com.submission.soilink.util.reduceFileImage
import com.submission.soilink.util.showLocation
import com.submission.soilink.util.showToast
import com.submission.soilink.util.uriToFile
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.about.AboutActivity
import com.submission.soilink.view.profile.ProfileActivity
import com.submission.soilink.view.result.ResultActivity
import com.submission.soilink.view.soillist.SoilListActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.Locale

/**
 * 3. get Lokasi, dan tanggal untuk dipush ke cloud (result activity) , izin penyimpanan, cek internet untuk halaman lainnya, hapus respon yg ga kepake
 * 4. result page
 * 5. history page
 *
 * 1. hapus string tidak kepake, log, dan lainlain
 * 2. cek koneksi ketika tidak ada koneksi aplikasi tidak ada internet (halaman soillist, deskripsinya, dan history)
 * 3. cek string
 * 4. testing bersama
 * */
class HomeFragment : Fragment(), LocationListener {

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null
    private lateinit var locationManager: LocationManager
    private var latitude: Double? = null
    private var longitude: Double? = null
    private val locationPermissionCode = 2

    private lateinit var networkCheck: NetworkCheck
    private var internetResult = true


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
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        checkInternetConnection()
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
            if (internetResult) {
                getLocation()
                startGallery()
            } else {
                showToast(requireContext(), getString(R.string.no_internet_connection))
            }
        }

        binding.btnSoilList.setOnClickListener {
            val intent = Intent(activity, SoilListActivity::class.java)
            startActivity(intent)
        }

        binding.btnAboutSoilInk.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.soilInformationDescription.text = getString(R.string.soil_information_desctiprion)
        networkCheck = NetworkCheck(requireContext())
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
                                    val location = showLocation(context, latitude!!, longitude!!)
                                    showToast(context, location)
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

    private fun getLocation() {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5F, this)
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude

        locationManager.removeUpdates(this)
    }

    private fun checkInternetConnection() {
        networkCheck.observe(viewLifecycleOwner) { hasNetwork ->
            internetResult = hasNetwork
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