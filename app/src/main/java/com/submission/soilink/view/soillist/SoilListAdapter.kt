package com.submission.soilink.view.soillist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.submission.soilink.api.response.SoilListItems
import com.submission.soilink.databinding.ItemSoilListBinding
import com.submission.soilink.util.loadImage

class SoilListAdapter(
    private val onClick: (SoilListItems) -> Unit
): ListAdapter<SoilListItems, SoilListAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemSoilListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) as SoilListItems
        holder.bind(item)
    }

    inner class ViewHolder (private val binding: ItemSoilListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SoilListItems) {
            binding.ivSoilListItem.loadImage(item.gambar)
            binding.tvSoilListItem.text = item.nama
            binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SoilListItems>() {
            override fun areItemsTheSame(oldItem: SoilListItems, newItem: SoilListItems): Boolean {
                return oldItem.nama == newItem.nama
            }

            override fun areContentsTheSame(oldItem: SoilListItems, newItem: SoilListItems): Boolean {
                return oldItem == newItem
            }
        }
    }
}