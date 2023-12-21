package com.submission.soilink.view.result

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.submission.soilink.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityResultBinding
//    private var currentImageUri: Uri? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()

        // Menerima URI gambar dari intent
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

    companion object {
        const val EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI"
    }
}
