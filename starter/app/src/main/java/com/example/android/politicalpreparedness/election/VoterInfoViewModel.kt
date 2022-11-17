package com.example.android.politicalpreparedness.election

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.database.ElectionDao
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Election
import com.example.android.politicalpreparedness.network.models.VoterInfoResponse
import kotlinx.coroutines.launch

class VoterInfoViewModel(
    private val dataSource: ElectionDao,
    private val electionId: Int,
    private val country: String,
    private val state: String
) : ViewModel() {

    private val _voterInfo = MutableLiveData<VoterInfoResponse>()
    val voterInfo: LiveData<VoterInfoResponse>
        get() = _voterInfo

    private val _followState = MutableLiveData<FollowState>()
    val followState: LiveData<FollowState>
        get() = _followState

    //TODO defer
    init {
        viewModelScope.launch {
            val election: Election? = dataSource.getElectionById(electionId)
            _followState.value = if (election == null) {
                FollowState.FOLLOW
            } else {
                FollowState.UNFOLLOW
            }
            _voterInfo.value = CivicsApi.retrofitService.getVoterInfo(electionId, "$country,$state")
        }
    }

    fun toggleFollowElection() {
        viewModelScope.launch {
            if (_followState.value === FollowState.FOLLOW) {
                dataSource.saveElection(_voterInfo.value!!.election)
                _followState.value = FollowState.UNFOLLOW
            } else {
                dataSource.deleteElectionById(electionId)
                _followState.value = FollowState.FOLLOW
            }
        }
    }


}