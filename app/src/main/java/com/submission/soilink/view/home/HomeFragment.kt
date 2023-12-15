package com.submission.soilink.view.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.submission.soilink.R
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore
import com.submission.soilink.databinding.FragmentHomeBinding
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.profile.ProfileActivity
import com.submission.soilink.view.soillist.SoilListActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HomeFragment : Fragment() {

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    private  var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = UserPreference.getInstance(requireActivity().dataStore)
        val user = runBlocking { pref.getSession().first() }
//        ApiConfig.token = user.token

        setupAction()

        binding.thisUser.text = requireActivity().getString(R.string.hallo_user, user.name)
    }

    private fun setupAction() {
        val user = "Ilham Dhani"
        val toolbar = binding.topAppBar
        toolbar.title = activity?.getString(R.string.user_login, user)

        binding.btnProfile.setOnClickListener {
            val intent = Intent(activity,  ProfileActivity::class.java)
            intent.putExtra(ProfileActivity.DATA_PROFILE, user)
            startActivity(intent)
        }

        binding.btnSoilList.setOnClickListener {
            val intent = Intent(activity, SoilListActivity::class.java)
            startActivity(intent)
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