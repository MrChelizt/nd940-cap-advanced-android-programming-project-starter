package com.example.android.politicalpreparedness.election

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.politicalpreparedness.database.ElectionDao

class VoterInfoViewModelFactory(
    private val dataSource: ElectionDao,
    private val electionId: Int?,
    private val country: String?,
    private val state: String?
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoterInfoViewModel::class.java)) {
            return VoterInfoViewModel(dataSource, electionId, country, state) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}