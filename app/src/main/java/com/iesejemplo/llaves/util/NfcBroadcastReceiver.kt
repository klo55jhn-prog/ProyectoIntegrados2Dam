package com.iesejemplo.llaves.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

const val INTENT_ACTION_NFC_READ = "com.iesejemplo.llaves.INTENT_ACTION_NFC_READ"

@Composable
fun NfcBroadcastReceiver(
    onSuccess: (Tag) -> Unit
) {
    val context = LocalContext.current
    val currentOnSuccess by rememberUpdatedState(onSuccess)

    DisposableEffect(context) {
        val intentFilter = IntentFilter(INTENT_ACTION_NFC_READ)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.getParcelableCompatibility(NfcAdapter.EXTRA_TAG, Tag::class.java)?.let { tag ->
                    currentOnSuccess(tag)
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            intentFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}

@Suppress("DEPRECATION")
fun <T : Parcelable> Intent.getParcelableCompatibility(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, clazz)
    } else {
        getParcelableExtra(key)
    }
}
