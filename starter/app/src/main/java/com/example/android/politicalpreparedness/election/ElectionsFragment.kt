package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.example.android.politicalpreparedness.network.models.Election

class ElectionsFragment : Fragment() {

    private val viewModel: ElectionsViewModel by lazy {
        val context = requireNotNull(this.context) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(
            this,
            ElectionsViewModelFactory(ElectionDatabase.getInstance(context))
        ).get(ElectionsViewModel::class.java)
    }

    var upcomingElectionsRecyclerViewAdapter: ElectionListAdapter? = null

    var savedElectionsRecyclerViewAdapter: ElectionListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding: FragmentElectionBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        upcomingElectionsRecyclerViewAdapter = ElectionListAdapter(ElectionListener {
            navigateToVoterInfoScreen(it)
        })

        savedElectionsRecyclerViewAdapter = ElectionListAdapter(ElectionListener {
            navigateToVoterInfoScreen(it)
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

    private fun navigateToVoterInfoScreen(election: Election) {
        this.findNavController().navigate(
            ElectionsFragmentDirections.actionElectionsFragmentToVoterInfoFragment(
                election.id,
                election.division
            )
        )
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

    override fun onResume() {
        super.onResume()
        viewModel.refreshSavedElections()
    }

}