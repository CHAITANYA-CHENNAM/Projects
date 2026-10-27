
package com.custom.acertvlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val action = intent.action

        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != "com.custom.acertvlauncher.TEST_BOOT"
        ) {
            return
        }

        Log.d(
            "AcerLauncher",
            "BootReceiver executed: $action"
        )

        // Test broadcasts only verify the receiver.
        // They do not open the launcher.
        if (action == "com.custom.acertvlauncher.TEST_BOOT") {
            return
        }

        val launchIntent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(launchIntent)
        } catch (e: Exception) {
            Log.e(
                "AcerLauncher",
                "Unable to launch after boot",
                e
            )
        }
    }
}
