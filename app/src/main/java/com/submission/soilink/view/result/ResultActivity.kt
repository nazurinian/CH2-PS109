package com.submission.soilink.view.result

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.submission.soilink.databinding.ActivityResultBinding
import com.submission.soilink.util.EXTRA_IMAGE_URI

class ResultActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityResultBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()

        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)?.toUri()
        imageUriString.let { uri ->
            Log.d("Image URI", "showImage: $uri")
            // Menampilkan gambar di ImageView
            binding.ivResult.setImageURI(uri)
        }
    }

    private fun setupAction() {
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }
}
