package com.submission.soilink.view.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.submission.soilink.data.SoilListData
import com.submission.soilink.databinding.FragmentHistoryBinding
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.detailhistory.DetailHistoryActivity
import com.submission.soilink.view.detailhistory.DetailHistoryActivity.Companion.HISTORY

class HistoryFragment : Fragment() {

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(requireActivity())
    }

    private  var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)

        setupAction()
        showSoilList()
    }

    private fun setupAction() {}

    private fun showSoilList() {
        val layoutManager = LinearLayoutManager(activity)
        val historyAdapter = binding.rvHistoryList
        historyAdapter.layoutManager  = layoutManager

        val adapter = HistoryAdapter { soil ->
            val detailIntent = Intent(activity, DetailHistoryActivity::class.java)
            detailIntent.putExtra(HISTORY, soil)
            startActivity(detailIntent)
        }

        historyAdapter.adapter = adapter

        adapter.submitList(SoilListData().soilList())
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}