package com.example.android.politicalpreparedness.representative

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.*
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.adapter.setNewValue
import com.example.android.politicalpreparedness.representative.model.Representative
import com.google.android.material.snackbar.Snackbar
import java.util.ArrayList

class RepresentativeFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        private const val MOTION_LAYOUT_STATE = "motion_layout_state"
        private const val REPRESENTATIVE_RECYCLER_DATA_STATE = "representative_recycler_data_state"
        private const val REPRESENTATIVE_RECYCLER_SCROLL_STATE =
            "representative_recycler_scroll_state"
    }

    private lateinit var viewModel: RepresentativeViewModel
    private lateinit var binding: FragmentRepresentativeBinding

    private lateinit var representativeListAdapter: RepresentativeListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = RepresentativeViewModel()

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        representativeListAdapter = RepresentativeListAdapter()

        initializeLocationClient()

        binding.representativesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = representativeListAdapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.states,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.state.adapter = adapter
            binding.state.onItemSelectedListener = this
        }
        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            if (viewModel.validateAddressInfo()) {
                viewModel.fetchRepresentatives()
            } else {
                Snackbar.make(
                    requireView(),
                    this.getString(viewModel.showSnackbarInt.value!!),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            if (checkLocationPermissions()) {
                getLocation { address ->
                    viewModel.setAddressInfo(address)
                    binding.state.setNewValue(address.state)
                }
            }
        }

        if (savedInstanceState != null) {
            binding.representativeMotionLayout.transitionToState(
                savedInstanceState.getInt(
                    MOTION_LAYOUT_STATE
                )
            )

            if (!savedInstanceState.getParcelableArrayList<Representative>(
                    REPRESENTATIVE_RECYCLER_DATA_STATE
                ).isNullOrEmpty()
            ) {
                representativeListAdapter.representatives =
                    savedInstanceState.getParcelableArrayList<Representative>(
                        REPRESENTATIVE_RECYCLER_DATA_STATE
                    ) as List<Representative>
            }

            binding.representativesRecyclerView.layoutManager?.scrollToPosition(
                savedInstanceState.getInt(
                    REPRESENTATIVE_RECYCLER_SCROLL_STATE
                )
            )

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.representatives.observe(viewLifecycleOwner) { representatives ->
            representatives.apply {
                representativeListAdapter.representatives = representatives
            }
        }
    }

    override fun onItemSelected(
        adapterView: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        viewModel.state.value = adapterView?.selectedItem as String
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        viewModel.state.value = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionResultForLocation(requestCode, grantResults) { address ->
            viewModel.setAddressInfo(address)
            binding.state.setNewValue(address.state)
        }
    }


    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val currentMotionLayoutState = binding.representativeMotionLayout.currentState
        outState.putInt(MOTION_LAYOUT_STATE, currentMotionLayoutState)

        val recyclerViewData =
            (binding.representativesRecyclerView.adapter as RepresentativeListAdapter).representatives
        outState.putParcelableArrayList(
            REPRESENTATIVE_RECYCLER_DATA_STATE,
            ArrayList<Representative>(recyclerViewData)
        )

        val recyclerViewScrollState = binding.representativesRecyclerView.scrollState
        outState.putInt(REPRESENTATIVE_RECYCLER_SCROLL_STATE, recyclerViewScrollState)
    }
}