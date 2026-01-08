package com.aaditx23.phtask.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

open class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    open fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun observeNetworkState(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Check if this network actually has internet
                val hasInternet = connectivityManager.getNetworkCapabilities(network)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

                if (hasInternet) {
                    trySend(true)
                }
            }

            override fun onLost(network: Network) {
                trySend(isNetworkAvailable())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)


        trySend(isNetworkAvailable())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
