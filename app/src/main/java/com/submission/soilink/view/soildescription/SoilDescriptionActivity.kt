package com.submission.soilink.view.soildescription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.net.toUri
import com.submission.soilink.R
import com.submission.soilink.api.response.SoilListItems
import com.submission.soilink.data.model.SoilListModel
import com.submission.soilink.databinding.ActivitySoilDescriptionBinding
import com.submission.soilink.util.loadImage

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
        val data = intent.getParcelableExtra<SoilListItems>(SOIL_DATA)
        data?.apply {
            binding.ivSoilDescription.loadImage(data.gambar)
            binding.tvTitle.text = data.nama
            binding.tvDescription.text = data.deskripsi
        }
    }

    companion object {
        const val SOIL_DATA = "SOIL_DATA"
    }
}