package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener

class ElectionsFragment : Fragment() {

    private lateinit var viewModel: ElectionsViewModel

    var upcomingElectionsRecyclerViewAdapter: ElectionListAdapter? = null

    var savedElectionsRecyclerViewAdapter: ElectionListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel = ViewModelProvider(
            this,
            ElectionsViewModelFactory(ElectionDatabase.getInstance(requireContext()))
        ).get(ElectionsViewModel::class.java)

        val binding: FragmentElectionBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        //TODO: Link elections to voter info

        //TODO: Initiate recycler adapters
        upcomingElectionsRecyclerViewAdapter = ElectionListAdapter(ElectionListener {
            //TODO add navigation
        })

        savedElectionsRecyclerViewAdapter = ElectionListAdapter(ElectionListener {
            //TODO add navigation
        })

        binding.upcomingElectionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = upcomingElectionsRecyclerViewAdapter
        }
        binding.savedElectionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = savedElectionsRecyclerViewAdapter
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.upcomingElections.observe(viewLifecycleOwner) { elections ->
            elections.apply {
                upcomingElectionsRecyclerViewAdapter?.elections = elections
            }
        }
        viewModel.savedElections.observe(viewLifecycleOwner) { elections ->
            elections.apply {
                savedElectionsRecyclerViewAdapter?.elections = elections
            }
        }
    }

}