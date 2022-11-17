package com.example.android.politicalpreparedness.representative

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _representatives = MutableLiveData<List<Representative>>()
    val representatives: LiveData<List<Representative>>
        get() = _representatives

    //TODO defer
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
        validateAddressInfo()
        return Address(
            addressLine1.value!!,
            addressLine2.value,
            city.value!!,
            state.value!!,
            zip.value!!
        )
    }

    private fun validateAddressInfo(){
        //TODO validate address is null or not except line 2
    }

}
