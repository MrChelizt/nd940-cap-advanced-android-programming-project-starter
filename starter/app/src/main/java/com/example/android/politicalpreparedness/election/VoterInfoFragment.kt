package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.constraintlayout.widget.Group
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.*
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.google.android.material.snackbar.Snackbar

class VoterInfoFragment : Fragment() {

    private val viewModel: VoterInfoViewModel by lazy {
        val context = requireNotNull(this.context) {
            "You can only access the viewModel after onViewCreated()"
        }
        ViewModelProvider(
            this,
            VoterInfoViewModelFactory(
                ElectionDatabase.getInstance(requireContext()).electionDao,
                null,
                null,
                null
            )
        ).get(VoterInfoViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val electionId = VoterInfoFragmentArgs.fromBundle(requireArguments()).argElectionId
        val division = VoterInfoFragmentArgs.fromBundle(requireArguments()).argDivision

        initializeLocationClient()

        viewModel.electionId = electionId
        viewModel.country = division.country
        viewModel.state = division.state

        val binding: FragmentVoterInfoBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        viewModel.voterInfo.observe(viewLifecycleOwner, Observer { voterInfo ->
            if (voterInfo.state.isNullOrEmpty()) {
                binding.addressGroup.visibility = Group.GONE
            } else {
                binding.addressGroup.visibility = Group.VISIBLE
            }
        })

        binding.stateLocations.setOnClickListener {
            if (viewModel.voterInfo.value?.state.isNullOrEmpty()) {
                Snackbar.make(
                    requireView(),
                    this.getString(R.string.error_voting_location_url_undefined),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                loadURL(viewModel.voterInfo.value!!.state!!.first().electionAdministrationBody.votingLocationFinderUrl!!)
            }
        }

        binding.stateBallot.setOnClickListener {
            if (viewModel.voterInfo.value?.state.isNullOrEmpty()
                || viewModel.voterInfo.value?.state?.first()?.electionAdministrationBody?.ballotInfoUrl.isNullOrEmpty()
            ) {
                Snackbar.make(
                    requireView(),
                    this.getString(R.string.error_ballot_information_url_undefined),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                loadURL(viewModel.voterInfo.value!!.state!!.first().electionAdministrationBody.ballotInfoUrl!!)
            }
        }

        binding.followButton.setOnClickListener {
            viewModel.toggleFollowElection()
        }

        if (!viewModel.state.isNullOrEmpty()) {
            viewModel.refreshVoterInfo()
        } else {
            if (checkLocationPermissions()) {
                getLocation { address ->
                    viewModel.state = address.state
                    viewModel.refreshVoterInfo()
                }
            }
        }

        return binding.root
    }

    private fun loadURL(url: String) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionResultForLocation(requestCode, grantResults) { address ->
            viewModel.state = address.state
            viewModel.refreshVoterInfo()
        }
    }

}