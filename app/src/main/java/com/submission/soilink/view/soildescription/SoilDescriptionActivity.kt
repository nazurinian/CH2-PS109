package com.submission.soilink.view.soildescription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.submission.soilink.R
import com.submission.soilink.data.model.SoilListModel
import com.submission.soilink.databinding.ActivitySoilDescriptionBinding

class SoilDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoilDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoilDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        setupLayout()
    }

    private fun setupAction() {
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupLayout() {
        val data = intent.getParcelableExtra<SoilListModel>(SOIL_DATA)
        data?.apply {
            binding.ivSoilDescription.setImageResource(soilImage)
            binding.tvTitle.text = resources.getString(R.string.soil_title, getString(soilTitle))
            binding.tvDescription.text = "Masi Kosong woi"
        }
    }

    companion object {
        const val SOIL_DATA = "SOIL_DATA"
    }
}