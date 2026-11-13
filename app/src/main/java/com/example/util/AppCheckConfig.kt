package com.example.util

import android.content.Context
import android.content.Intent

/**
 * Configures Firebase App Check via [FirebaseManager] using the Activity launch intent
 * so debug tokens delivered via Intent extras are persisted synchronously before
 * any Firebase AI Logic SDK requests execute.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    FirebaseManager.initializeAppCheck(context, intent)
}
