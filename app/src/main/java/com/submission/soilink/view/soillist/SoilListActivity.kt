package com.submission.soilink.view.soillist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.submission.soilink.data.SoilListData
import com.submission.soilink.databinding.ActivitySoilListBinding
import com.submission.soilink.view.soildescription.SoilDescriptionActivity
import com.submission.soilink.view.soildescription.SoilDescriptionActivity.Companion.SOIL_DATA

class SoilListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoilListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoilListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        showSoilList()
    }

    private fun setupAction() {
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
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

        adapter.submitList(SoilListData().soilList())
    }
}