package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Central Firebase initialization manager that configures FirebaseAppCheck before
 * any Firebase AI Logic SDK calls occur.
 *
 * - Local development (BuildConfig.DEBUG): installs DebugAppCheckProviderFactory and
 *   synchronously persists any launch-Intent FIREBASE_APPCHECK_DEBUG_TOKEN before factory install.
 * - Production builds (!BuildConfig.DEBUG): installs PlayIntegrityAppCheckProviderFactory.
 */
object FirebaseManager {

    private const val TAG = "FirebaseManager"
    private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
    private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
    private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"

    @Volatile
    private var appCheckInitialized: Boolean = false

    fun initializeAppCheck(context: Context, intent: Intent? = null) {
        try {
            val appContext = context.applicationContext ?: context
            if (FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext)
            }

            if (BuildConfig.DEBUG) {
                val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
                if (!debugToken.isNullOrEmpty()) {
                    val persistenceKey = FirebaseApp.getInstance().persistenceKey
                    val prefs = appContext.getSharedPreferences(
                        "$DEBUG_STORE_PREFIX$persistenceKey",
                        Context.MODE_PRIVATE
                    )
                    // Synchronous commit() prevents a race condition where
                    // DebugAppCheckProviderFactory reads SharedPreferences before apply() flushes.
                    prefs.edit()
                        .putString(DEBUG_SECRET_KEY, debugToken)
                        .commit()
                    Log.d(TAG, "App Check debug secret configured successfully.")
                } else {
                    Log.w(TAG, "No debug token found in Intent extra.")
                }

                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "DebugAppCheckProviderFactory installed for local development.")
            } else {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "PlayIntegrityAppCheckProviderFactory installed for production build.")
            }
            appCheckInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
        }
    }

    fun isAppCheckInitialized(): Boolean = appCheckInitialized
}
