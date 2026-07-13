package com.videosdkconnectionservice.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tylerthrailkill.helpers.prettyprint.pp
import com.videosdkconnectionservice.viewmodel.MyConnectionService

class StartCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        pp("StartCallReceiver - Received broadcast: ${intent?.action}")

        if (intent?.action == "com.videosdkconnectionservice.ACTION_START_CALL") {
            pp("Starting Call from BroadcastReceiver...")
            val service = MyConnectionService.getInstance()
            service.triggerStartCall()
            return
        }
    }
}


