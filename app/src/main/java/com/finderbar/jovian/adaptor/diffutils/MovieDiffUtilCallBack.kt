package com.finderbar.jovian.adaptor.diffutils

import android.support.v7.util.DiffUtil
import com.finderbar.jovian.models.Movie

class MovieDiffUtilCallBack : DiffUtil.ItemCallback<Movie>() {
    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }
}