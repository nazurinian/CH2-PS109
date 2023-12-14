package com.submission.soilink.data

import com.submission.soilink.R
import com.submission.soilink.data.model.SoilListModel

class SoilListData {
    fun soilList(): List<SoilListModel> {
        return listOf(
            SoilListModel(1, R.string.soil_inceptisol, R.drawable.inceptisol, ""),
            SoilListModel(2, R.string.soil_kapur, R.drawable.kapur, ""),
            SoilListModel(3, R.string.soil_pasir, R.drawable.pasir, ""),
            SoilListModel(4, R.string.soil_laterit, R.drawable.laterit, ""),
            SoilListModel(5, R.string.soil_humus, R.drawable.humus, ""),
            SoilListModel(6, R.string.soil_aluvial, R.drawable.aluvial, ""),
            SoilListModel(7, R.string.soil_andosol, R.drawable.andosol, ""),
            SoilListModel(8, R.string.soil_entisol, R.drawable.entisol, ""),
        )
    }
}