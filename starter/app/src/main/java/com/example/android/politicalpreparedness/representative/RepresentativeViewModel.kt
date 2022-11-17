package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel : ViewModel() {

    val addressLine1 = MutableLiveData<String>()
    val addressLine2 = MutableLiveData<String>()
    val city = MutableLiveData<String>()
    val state = MutableLiveData<String>()
    val zip = MutableLiveData<String>()
    val showSnackbarInt = MutableLiveData<Int>()

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    fun fetchRepresentatives() {
        viewModelScope.launch {
            refreshRepresentatives()
        }
    }

    private suspend fun refreshRepresentatives() {
        val address: Address = getAddressInfo()
        val (offices, officials) = CivicsApi.retrofitService.getRepresentatives(address.toFormattedString())
            .await()
        _representatives.value =
            offices.flatMap { office -> office.getRepresentatives(officials) }
    }

    fun setAddressInfo(address: Address) {
        addressLine1.value = address.line1
        addressLine2.value = address.line2
        city.value = address.city
        state.value = address.state
        zip.value = address.zip
    }

    private fun getAddressInfo(): Address {
        return Address(
            addressLine1.value!!,
            addressLine2.value,
            city.value!!,
            state.value!!,
            zip.value!!
        )
    }

    fun validateAddressInfo(): Boolean {
        if (addressLine1.value.isNullOrEmpty()) {
            showSnackbarInt.value = R.string.error_address_line_1_required
        } else if (city.value.isNullOrEmpty()) {
            showSnackbarInt.value = R.string.error_city_required
        } else if (state.value.isNullOrEmpty()) {
            showSnackbarInt.value = R.string.error_state_required
        } else if (zip.value.isNullOrEmpty()) {
            showSnackbarInt.value = R.string.error_zip_required
        } else {
            return true
        }
        return false
    }

}
