package com.submission.soilink.data

import com.submission.soilink.R
import com.submission.soilink.data.model.SoilListModel

class SoilListData {
    fun soilList(): List<SoilListModel> {
        return listOf(
            SoilListModel(R.string.soil_inceptisol, R.drawable.inceptisol),
            SoilListModel(R.string.soil_kapur, R.drawable.kapur),
            SoilListModel(R.string.soil_pasir, R.drawable.pasir),
            SoilListModel(R.string.soil_laterit, R.drawable.laterit),
            SoilListModel(R.string.soil_humus, R.drawable.humus),
            SoilListModel(R.string.soil_aluvial, R.drawable.aluvial),
            SoilListModel(R.string.soil_andosol, R.drawable.andosol),
            SoilListModel(R.string.soil_entisol, R.drawable.entisol),
        )
    }
}