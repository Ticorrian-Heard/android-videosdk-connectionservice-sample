package com.videosdkconnectionservice.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.videosdkconnectionservice.viewmodel.MyConnectionService
import com.videosdkconnectionservice.viewmodel.ZoomSessionViewModel

class StartCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("StartCallReceiver", "Received broadcast: ${intent?.action}")
        
        if (context == null || intent == null) {
            Log.e("StartCallReceiver", "Context or intent is null")
            return
        }

        if (intent.action != "com.videosdkconnectionservice.ACTION_START_CALL") {
            Log.w("StartCallReceiver", "Unknown action: ${intent.action}")
            return
        }

        // Try connection service first
        val service = MyConnectionService.getInstance()
        if (service != null) {
            Log.d("StartCallReceiver", "Triggering call via MyConnectionService")
            service.triggerStartCall()
            return
        }

        // Fallback to ViewModel
        val vm = ZoomSessionViewModel.instance
        if (vm != null) {
            Log.d("StartCallReceiver", "Triggering call via ZoomSessionViewModel")
            vm.startCall()
        } else {
            Log.e("StartCallReceiver", "No service or ViewModel available")
        }
    }
}


