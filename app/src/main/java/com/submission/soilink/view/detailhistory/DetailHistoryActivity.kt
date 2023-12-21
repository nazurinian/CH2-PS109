package com.submission.soilink.view.detailhistory

import android.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.submission.soilink.databinding.ActivityDetailHistoryBinding

class DetailHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailHistoryBinding.inflate(layoutInflater)
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

        binding.share.setOnClickListener {
            //contoh share ke wa
//            val phoneNumber = "1234567890"
            val message = "Details history"
//            val whatsappLink = "https://api.whatsapp.com/send?phone=${phoneNumber}&amp;text=${message}"
////            val whatsappLink = getString(R.string.whatsapp_link, phoneNumber, message)
//
//            val uri = Uri.parse(whatsappLink)
//            val intent = Intent(Intent.ACTION_VIEW, uri)
//            startActivity(intent)

            //contoh share ke pesan pokoknya
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }

            val chooser = Intent.createChooser(sendIntent, title)
            startActivity(chooser)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val HISTORY = "HISTORY"
    }
}