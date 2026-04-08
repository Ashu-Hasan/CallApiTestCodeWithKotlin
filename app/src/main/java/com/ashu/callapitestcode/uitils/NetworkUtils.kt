package com.ashu.callapitestcode.uitils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object NetworkUtils {

    // 🔹 Old method
    fun connected(context: Context): Boolean {
        val manager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return manager.activeNetworkInfo?.isConnectedOrConnecting == true
    }

    // 🔹 Modern method
    fun isInternetAvailable(context: Context?): Boolean {

        if (context == null) return false

        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
                ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            capabilities?.let {
                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false

        } else {
            try {
                cm.activeNetworkInfo?.isConnected == true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}