package com.submission.soilink.view.soildescription

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.submission.soilink.databinding.ActivitySoilDescriptionBinding

class SoilDescriptionActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySoilDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySoilDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    companion object {
        const val SOIL_DATA = "SOIL_DATA"
    }
}