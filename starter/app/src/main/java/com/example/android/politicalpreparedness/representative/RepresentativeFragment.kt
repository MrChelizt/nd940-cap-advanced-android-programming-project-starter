package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.example.android.politicalpreparedness.representative.adapter.setNewValue
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class RepresentativeFragment : Fragment(), AdapterView.OnItemSelectedListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 34
    }

    private lateinit var viewModel: RepresentativeViewModel
    private lateinit var binding: FragmentRepresentativeBinding

    private lateinit var representativeListAdapter: RepresentativeListAdapter

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = RepresentativeViewModel()

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        representativeListAdapter = RepresentativeListAdapter()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

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
            viewModel.fetchRepresentatives()
        }

        binding.buttonLocation.setOnClickListener {
            hideKeyboard()
            if (checkLocationPermissions()) {
                getLocation()
            }
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

    //TODO check this if we can use bind adapter instead of this
    override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
        viewModel.state.value = adapterView?.selectedItem as String
    }

    //TODO check this if we can use bind adapter instead of this
    override fun onNothingSelected(p0: AdapterView<*>?) {
        viewModel.state.value = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults.first() == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            } else {
                Snackbar.make(
                    requireView(),
                    R.string.error_permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            Log.e("Request location", "permissions")
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getLocation() {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val address = geoCodeLocation(task.result!!)
                    viewModel.setAddressInfo(address)
                    binding.state.setNewValue(address.state)
                } else {
                    Snackbar.make(
                        requireView(),
                        R.string.error_location_required, Snackbar.LENGTH_INDEFINITE
                    ).setAction(R.string.location_settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
                }
            }
        } catch (e: SecurityException) {
            Snackbar.make(
                requireView(),
                e.message ?: "Security Exception",
                Snackbar.LENGTH_LONG
            ).show()
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun geoCodeLocation(location: Location): Address {
        val geocoder = Geocoder(context, Locale.getDefault())
        return geocoder.getFromLocation(location.latitude, location.longitude, 1)
            .map { address ->
                Address(
                    address.thoroughfare,
                    address.subThoroughfare,
                    address.locality,
                    address.adminArea,
                    address.postalCode
                )
            }
            .first()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.windowToken, 0)
    }

}