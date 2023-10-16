package com.finderbar.jovian.datasource.user

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.finderbar.jovian.datasource.convertUser
import com.finderbar.jovian.models.*
import com.finderbar.jovian.utilities.NetworkState
import query.AllUsersQuery

/**
 * Created by thein on 1/12/19.
 */
class UserDataSource(private val apolloClient: ApolloClient, private val query: String):
        PageKeyedDataSource<Int, User>() {
    val networkState = MutableLiveData<NetworkState>()
    val loadingInitial = MutableLiveData<Boolean>()
    val loadingBefore = MutableLiveData<Boolean>()
    val loadingAfter = MutableLiveData<Boolean>()

    override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, User>) {
        load(params.requestedLoadSize, 0, loadingInitial) { result ->
            if (result.hasNext) {
                callback.onResult(result.users, null, result.users.size)
            } else {
                callback.onResult(result.users, null, null)
            }
        }
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
        load(params.requestedLoadSize, params.key, loadingAfter) { result ->
            if (result.hasNext) {
                callback.onResult(result.users, params.key + result.users.size)
            } else {
                callback.onResult(result.users, null)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, User>) {
        load(params.requestedLoadSize, params.key, loadingBefore) { result ->
            if (result.hasNext) {
                callback.onResult(result.users, params.key - result.users.size)
            } else {
                callback.onResult(result.users, null)
            }
        }
    }

    private fun load(pageLimit: Int, pageSkip: Int, loadingLiveData: MutableLiveData<Boolean>, processResult: (UserSearchResult) -> Unit ) {
        val query = setUserQueryOffset(InputCriteria("", query, pageLimit, pageSkip ))
        loadingLiveData.postValue(true)
        networkState.postValue(NetworkState.FETCHING)

        apolloClient.query(query).responseFetcher(ApolloResponseFetchers.NETWORK_FIRST)
        .enqueue(object : ApolloCall.Callback<AllUsersQuery.Data>() {
            override fun onResponse(response: Response<AllUsersQuery.Data>) {
                val error = response.data()?.allUsers()?.error();
                if (error !== null) {
                    networkState.postValue(NetworkState.FAILURE)
                } else {
                    val result = convertUser(response.data()?.allUsers()?.users()!!)
                    val hasNext = response.data()?.allUsers()?.hasNext();
                    val totalCount = response.data()?.allUsers()?.totalCount()
                    networkState.postValue(NetworkState.SUCCESS)
                    processResult(UserSearchResult(result, hasNext!!, totalCount!!.toLong()))
                }
            }
            override fun onFailure(e: ApolloException) {
                networkState.postValue(NetworkState.FAILURE)
            }
        })

        loadingLiveData.postValue(false)
    }

    private fun setUserQueryOffset(criteria: InputCriteria) = AllUsersQuery.builder().criteria(criteria.get()).build()


}
