package com.submission.soilink.view.soillistdetail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.submission.soilink.R

class SoilListDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_soil_list_detail)
    }

    companion object {
        const val SOIL_ID = "SOLID_ID"
    }
}