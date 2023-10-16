package com.finderbar.jovian.datasource.discuss

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.arch.paging.DataSource
import com.apollographql.apollo.ApolloClient
import com.finderbar.jovian.utilities.NetworkState
import com.finderbar.jovian.models.User

class AnswerDownVoteDataSourceFactory(private val apolloClient: ApolloClient): DataSource.Factory<Int, User>() {

    private val lastDataSource = MutableLiveData<AnswerDownVoteDataSource>()
    val loadingInitial: LiveData<Boolean> = Transformations.switchMap(lastDataSource) { dataSource -> dataSource.loadingInitial }
    val loadingBefore: LiveData<Boolean> = Transformations.switchMap(lastDataSource) { dataSource -> dataSource.loadingBefore }
    val loadingAfter: LiveData<Boolean> = Transformations.switchMap(lastDataSource) { dataSource -> dataSource.loadingAfter }
    val emptyState: LiveData<Boolean> = Transformations.switchMap(lastDataSource) { dataSource -> dataSource.emptyState }
    val networkState: LiveData<NetworkState> = Transformations.switchMap(lastDataSource) { dataSource -> dataSource.networkState }
    var answerId: String = ""

    override fun create(): DataSource<Int, User> {
        return AnswerDownVoteDataSource(apolloClient, answerId).also { lastDataSource.postValue(it) }
    }

    fun refresh() {
        lastDataSource.value?.invalidate()
    }

}