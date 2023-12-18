package com.submission.soilink.view.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submission.soilink.data.model.SoilListModel
import com.submission.soilink.databinding.ItemHistoryBinding

class HistoryAdapter (
    private val onClick: (SoilListModel) -> Unit
): ListAdapter<SoilListModel, HistoryAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) as SoilListModel
        holder.bind(item)
    }

    inner class ViewHolder (private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SoilListModel) {
            binding.ivHistory.setImageResource(item.soilImage)
            binding.typeSoil.setText(item.soilTitle)
            binding.tvDescription.text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis ac ante tortor. Duis suscipit leo nec ante fringilla, in convallis ligula pellentesque. Praesent finibus felis in arcu rutrum, nec mollis magna tincidunt. Pellentesque sit amet blandit felis, ac scelerisque ligula. Vestibulum quis lorem sit amet lorem aliquam porta. In vehicula mi at elit venenatis elementum. Sed tempor a felis vitae blandit."
            binding.tvTime.text = "24, January 2024"
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