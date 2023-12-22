package com.submission.soilink.view.result

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.submission.soilink.data.model.PostHistoryModel
import com.submission.soilink.databinding.ActivityResultBinding
import com.submission.soilink.util.showLocation
import com.submission.soilink.view.home.HomeActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
    }

    private fun setupAction() {
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        val result = intent.getParcelableExtra<PostHistoryModel>(RESULT)
        val soilType = binding.resultSoilType
        val soilDesctiption = binding.resultSoilTypeDescription
        val note = binding.note
        val date = binding.date
        val location = binding.location


        result?.image?.toUri().let { uri ->
            binding.ivResult.setImageURI(uri)
        }
        date.text = result?.dateTime
        location.text = showLocation(this, result?.lat, result?.long)

        binding.btnSave.setOnClickListener {
            //proses save ke history
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    companion object {
        const val EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI"
        const val RESULT = "RESULT"
    }
}
