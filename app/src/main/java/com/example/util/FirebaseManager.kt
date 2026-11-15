package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

/**
 * Central Firebase initialization manager that configures FirebaseAppCheck before
 * any Firebase AI Logic SDK calls occur.
 *
 * - Local development (BuildConfig.DEBUG): installs DebugAppCheckProviderFactory and
 *   synchronously persists any launch-Intent FIREBASE_APPCHECK_DEBUG_TOKEN before factory install.
 * - Production builds (!BuildConfig.DEBUG): installs RecaptchaAppCheckProviderFactory using
 *   the provisioned `recaptcha_site_key` resource (falling back to PlayIntegrityAppCheckProviderFactory).
 */
object FirebaseManager {

    private const val TAG = "FirebaseManager"
    private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
    private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
    private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
    private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

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
                val resId = appContext.resources.getIdentifier(
                    RECAPTCHA_SITE_KEY_RES,
                    "string",
                    appContext.packageName
                )
                val siteKey = if (resId != 0) appContext.getString(resId) else ""
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed for production build.")
                } else {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        PlayIntegrityAppCheckProviderFactory.getInstance()
                    )
                    Log.d(TAG, "PlayIntegrityAppCheckProviderFactory installed for production build.")
                }
            }
            appCheckInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
        }
    }

    fun isAppCheckInitialized(): Boolean = appCheckInitialized

    /**
     * Reads the active App Check debug UUID persisted by [DebugAppCheckProviderFactory]
     * in SharedPreferences so users sideloading a Debug APK on a physical device can copy
     * and register it in Firebase Console -> App Check -> Manage debug tokens without needing adb logcat.
     */
    fun getDebugSecret(context: Context): String? {
        if (!BuildConfig.DEBUG) return null
        return try {
            val appContext = context.applicationContext ?: context
            if (FirebaseApp.getApps(appContext).isEmpty()) return null
            val persistenceKey = FirebaseApp.getInstance().persistenceKey
            val prefs = appContext.getSharedPreferences(
                "$DEBUG_STORE_PREFIX$persistenceKey",
                Context.MODE_PRIVATE
            )
            prefs.getString(DEBUG_SECRET_KEY, null)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to read App Check debug secret", e)
            null
        }
    }

    /**
     * Ensures a debug UUID is generated (if missing) by requesting an App Check token,
     * then returns the persisted local debug UUID via [onResult].
     */
    fun fetchOrCreateDebugSecret(context: Context, onResult: (String?) -> Unit) {
        if (!BuildConfig.DEBUG) {
            onResult(null)
            return
        }
        val existing = getDebugSecret(context)
        if (!existing.isNullOrBlank()) {
            onResult(existing)
            return
        }
        try {
            if (!appCheckInitialized) {
                initializeAppCheck(context, null)
            }
            FirebaseAppCheck.getInstance()
                .getAppCheckToken(false)
                .addOnCompleteListener {
                    onResult(getDebugSecret(context))
                }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to trigger App Check debug secret generation", e)
            onResult(getDebugSecret(context))
        }
    }

    fun isAppCheckError(errorMessage: String?): Boolean {
        if (errorMessage.isNullOrBlank()) return false
        return errorMessage.contains("App Check", ignoreCase = true) ||
            errorMessage.contains("attestation", ignoreCase = true) ||
            errorMessage.contains("HTTP 403", ignoreCase = true)
    }
}
