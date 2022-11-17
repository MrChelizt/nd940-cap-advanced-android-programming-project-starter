package com.example.android.politicalpreparedness.election.adapter

import android.widget.Button
import androidx.databinding.BindingAdapter
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.election.FollowState

@BindingAdapter("followText")
fun setFollowButtonText(view: Button, state: FollowState?) {
    if (state == FollowState.UNFOLLOW) {
        view.text = view.context.getString(R.string.unfollow_election)
    } else {
        view.text = view.context.getString(R.string.follow_election)
    }
}