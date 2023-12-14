package com.submission.soilink.view.soillist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submission.soilink.data.model.SoilListModel
import com.submission.soilink.databinding.ItemSoilListBinding

class SoilListAdapter(
    private val onClick: (SoilListModel) -> Unit
): ListAdapter<SoilListModel, SoilListAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemSoilListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) as SoilListModel
        holder.bind(item)
    }

    inner class ViewHolder (private val binding: ItemSoilListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SoilListModel) {
            binding.ivSoilListItem.setImageResource(item.soilImage)
            binding.tvSoilListItem.setText(item.soilTitle)
            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SoilListModel>() {
            override fun areItemsTheSame(oldItem: SoilListModel, newItem: SoilListModel): Boolean {
                return oldItem.soilTitle == newItem.soilTitle
            }

            override fun areContentsTheSame(oldItem: SoilListModel, newItem: SoilListModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}