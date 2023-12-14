package com.submission.soilink.view.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.submission.soilink.R
import com.submission.soilink.api.ApiConfig
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore
import com.submission.soilink.databinding.ActivityHomeBinding
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.login.LoginActivity
import com.submission.soilink.view.profile.ProfileActivity
import com.submission.soilink.view.profile.ProfileActivity.Companion.DATA_PROFILE
import com.submission.soilink.view.soillist.SoilListActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class HomeActivity : AppCompatActivity() {

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = UserPreference.getInstance(this.dataStore)
        val user = runBlocking { pref.getSession().first() }
//        ApiConfig.token = user.token

        binding.thisUser.text = applicationContext.getString(R.string.hallo_user, user.name)

        setupAction()
    }

    private fun setupAction() {
        val user = "Ilham Dhani"
        val toolbar = binding.topAppBar
        toolbar.title = resources.getString(R.string.user_login, user)

        binding.btnProfile.setOnClickListener {
            val intent = Intent(this,  ProfileActivity::class.java)
            intent.putExtra(DATA_PROFILE, user)
            startActivity(intent)
        }

        binding.btnSoilList.setOnClickListener {
            val intent = Intent(this, SoilListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val USER_NAME = "USER_NAME"
    }
}
