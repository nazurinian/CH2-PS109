package com.submission.soilink.view.soillist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.databinding.ActivitySoilListBinding
import com.submission.soilink.util.NetworkCheck
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.soildescription.SoilDescriptionActivity
import com.submission.soilink.view.soildescription.SoilDescriptionActivity.Companion.SOIL_DATA

class SoilListActivity : AppCompatActivity() {

    private val viewModel by viewModels<SoilListViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivitySoilListBinding
    private lateinit var networkCheck: NetworkCheck

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoilListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        networkCheck.observe(this) { hasNetwork ->
            if (hasNetwork) {
                lostConnection(false)
                showSoilList()
            } else {
                lostConnection(true)
            }
        }
    }

    private fun setupAction() {
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        networkCheck = NetworkCheck(this)
    }

    private fun showSoilList() {
        val layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        val soilAdapter = binding.rvSoilList
        soilAdapter.layoutManager  = layoutManager

        val adapter = SoilListAdapter { soil ->
            val detailIntent = Intent(this, SoilDescriptionActivity::class.java)
            detailIntent.putExtra(SOIL_DATA, soil)
            startActivity(detailIntent)
        }

        soilAdapter.adapter = adapter
        viewModel.getSoilList().observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    showLoading(true)
                }

                is ResultState.Success -> {
                    showLoading(false)
                    val data = result.data
                    Log.d("Anjir", data.toString())
                    adapter.submitList(data)
                }

                is ResultState.Error -> {
                    showLoading(false)
                    showToast(this, result.error)
                }
            }
        }
    }

    private fun lostConnection(isLost: Boolean) {
        binding.dataContainer.visibility = if (isLost) View.GONE else View.VISIBLE
        binding.internetLost.visibility = if (isLost) View.VISIBLE else View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}