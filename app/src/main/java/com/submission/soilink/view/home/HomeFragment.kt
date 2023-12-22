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
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.model.PostHistoryModel
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore
import com.submission.soilink.databinding.FragmentHomeBinding
import com.submission.soilink.util.NetworkCheck
import com.submission.soilink.util.Permission
import com.submission.soilink.util.REQUIRED_COARSE_LOCATION_PERMISSION
import com.submission.soilink.util.REQUIRED_FINE_LOCATION_PERMISSION
import com.submission.soilink.util.accountName
import com.submission.soilink.util.getCurrentDate
import com.submission.soilink.util.reduceFileImage
import com.submission.soilink.util.showLocation
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
class HomeFragment : Fragment() {

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
    private lateinit var permission: Permission
    private var internetResult = true

    private lateinit var fusedLocationClient: FusedLocationProviderClient


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

        permission = Permission(requireActivity()) {
            if (internetResult) {
                requestLocationUpdates()
                startGallery()
            } else {
                showToast(requireContext(), getString(R.string.no_internet_connection))
            }
        }

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
            Permission.handlePermissionFlow(permission)
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
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
        val pref = UserPreference.getInstance(requireActivity().dataStore)
        val user = runBlocking { pref.getSession().first() }

        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, requireContext()).reduceFileImage()

            AlertDialog.Builder(requireContext()).apply {
                setTitle(getString(R.string.check_soil_type))
                setCancelable(false)
                setNegativeButton(getString(R.string.btn_yes)) { _, _ ->
                    showToast(requireContext(), getString(R.string.start_check))

                    viewModel.locationUpdated.observe(requireActivity()) { locationResult ->
                        if (locationResult) {
                            val postHistory = PostHistoryModel(
                                email = user.email,
                                image = imageFile,
                                dateTime = getCurrentDate(),
                                lat = latitude,
                                long = longitude
                            )

                            viewModel.addHistory(postHistory).observe(requireActivity()) { result ->
                                if (result != null) {
                                    when (result) {
                                        is ResultState.Loading -> {
                                            showLoading(true)
                                            activity?.window?.setFlags(
                                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                            )
                                        }

                                        is ResultState.Success -> {
                                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                            showToast(context, getString(R.string.post_successfull))
                                            showLoading(false)
                                            val intent = Intent(context, ResultActivity::class.java)
                                            intent.putExtra(ResultActivity.RESULT, postHistory)
                                            startActivity(intent)
                                        }

                                        is ResultState.Error -> {
                                            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                            showToast(requireContext(), result.error)
                                            showLoading(false)
                                        }
                                    }
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

    @Suppress("DEPRECATION")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.lastLocation?.let { location ->
                    latitude = location.latitude
                    longitude = location.longitude

                    viewModel.setLocation(true)

                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                REQUIRED_COARSE_LOCATION_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                REQUIRED_FINE_LOCATION_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(REQUIRED_COARSE_LOCATION_PERMISSION),
                locationPermissionCode
            )

            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(REQUIRED_FINE_LOCATION_PERMISSION),
                locationPermissionCode
            )
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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