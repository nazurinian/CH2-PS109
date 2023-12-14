package com.submission.soilink.view.soillist

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.submission.soilink.R
import com.submission.soilink.databinding.ActivitySoilListBinding
import com.submission.soilink.view.soillistdetail.SoilListDetailActivity

class SoilListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoilListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoilListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.rvSoilList.layoutManager = layoutManager

        showSoilList()
    }

    private fun showSoilList() {
        val adapter = SoilListAdapter { soil ->
            val detailIntent = Intent(this, SoilListDetailActivity::class.java)
            detailIntent.putExtra(SOIL_ID, soil.id)
            startActivity(detailIntent)
        }
        viewModel.habits.observe(this) { habit ->
            adapter.submitList(habit)
        }
        recycler.adapter = adapter
    }
}