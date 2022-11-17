package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDatabase
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import kotlinx.coroutines.launch

class ElectionsViewModel(private val dataSource: ElectionDatabase) : ViewModel() {

    private val _upcomingElections = MutableLiveData<List<Election>>()
    val upcomingElections: LiveData<List<Election>>
        get() = _upcomingElections

    private val _savedElections = MutableLiveData<List<Election>>()
    val savedElections: LiveData<List<Election>>
        get() = _savedElections

    init {
        viewModelScope.launch {
            _upcomingElections.value = CivicsApi.retrofitService.getElections().await().elections
            _savedElections.value = dataSource.electionDao.getElections()
        }
    }

    fun refreshSavedElections() {
        viewModelScope.launch {
            _savedElections.value = dataSource.electionDao.getElections()
        }
    }

}