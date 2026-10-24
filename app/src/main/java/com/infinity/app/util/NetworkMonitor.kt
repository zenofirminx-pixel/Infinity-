package com.infinity.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

class NetworkMonitor(context: Context) {
    private val manager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    fun isOnline(): Boolean = manager.activeNetwork?.let { network ->
        manager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } ?: false

    fun register(whenAvailable: () -> Unit, whenLost: () -> Unit): ConnectivityManager.NetworkCallback {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = whenAvailable.invoke()
            override fun onLost(network: Network) = whenLost.invoke()
        }
        manager.registerNetworkCallback(NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), callback)
        return callback
    }
    fun unregister(callback: ConnectivityManager.NetworkCallback) { runCatching { manager.unregisterNetworkCallback(callback) } }
}
