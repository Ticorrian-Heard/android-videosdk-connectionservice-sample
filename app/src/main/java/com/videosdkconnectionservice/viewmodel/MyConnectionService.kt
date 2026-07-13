package com.videosdkconnectionservice.viewmodel

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.tylerthrailkill.helpers.prettyprint.pp

class MyConnectionService : ConnectionService() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: MyConnectionService? = null
        fun getInstance(): MyConnectionService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MyConnectionService().also { INSTANCE = it }
            }
        }
    }
    private lateinit var context: Context
    private lateinit var zoomViewModel: ZoomSessionViewModel
    private lateinit var phoneAccountHandle: PhoneAccountHandle

    fun setParams(context: Context, zoomViewModel: ZoomSessionViewModel){
        this.context = context
        this.zoomViewModel = zoomViewModel
    }
    fun registerTelecomAccount() {
        if (!::context.isInitialized) {
            pp("Error:" + "Context is missing!")
            return
        }
        val telecomManager = context.getSystemService(TELECOM_SERVICE) as TelecomManager
        val componentName = ComponentName(context, MyConnectionService::class.java)
        phoneAccountHandle = PhoneAccountHandle(componentName, context.packageName)

        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "My VoIP App")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER or PhoneAccount.CAPABILITY_VIDEO_CALLING)
            .addSupportedUriScheme(PhoneAccount.SCHEME_TEL)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
        pp("Phone account registered successfully")
    }

    /**
    * This step would in practice be triggered by a push notification from a secure server.
    */
    fun addNewIncomingCall(
        phoneNumberOrCallID: String = "Zoom Session",
    ) {
        if (!this::context.isInitialized || !this::phoneAccountHandle.isInitialized) {
            pp("Error: Context and phoneAccountHandle must be initialized. Call setParams() and registerTelecomAccount() first.")
            return
        }
        val telecomManager = context.getSystemService(TELECOM_SERVICE) as TelecomManager

        val extras = Bundle().apply {
            val uri = Uri.fromParts(PhoneAccount.SCHEME_TEL, phoneNumberOrCallID, null)
            putParcelable(TelecomManager.EXTRA_INCOMING_CALL_ADDRESS, uri)
            putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle)
        }

        try {
            telecomManager.addNewIncomingCall(phoneAccountHandle, extras)
            pp("System-managed incoming call flow triggered - onCreateIncomingConnection will be called")
        } catch (e: SecurityException) {
            pp("SecurityException: Unable to trigger incoming call: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Triggered externally (e.g. receiver or adb broadcast) to start the scheduled call flow.
     */
    fun triggerStartCall() {
        if (!this::zoomViewModel.isInitialized) {
            pp("Cannot trigger startCall: zoomViewModel not initialized")
            return
        }
        try {
            zoomViewModel.startCall()
        } catch (e: Exception) {
            pp("Error triggering startCall: ${e.message}")
        }
    }

    override fun onCreateIncomingConnection(
        handle: PhoneAccountHandle,
        request: ConnectionRequest
    ): Connection {
        // Try to use the initialized singleton instance if this instance isn't initialized
        val serviceInstance = if (!this::zoomViewModel.isInitialized) {
            val singletonInstance = getInstance()
            if (singletonInstance::zoomViewModel.isInitialized) {
                singletonInstance
            } else {
                pp("Error: zoomViewModel has not been initialized on any service instance.")
                throw IllegalStateException("zoomViewModel must be initialized before creating incoming connection")
            }
        } else {
            this
        }

        val connection = ZoomVoIPConnection(serviceInstance.zoomViewModel)
        connection.setAddress(request.address, TelecomManager.PRESENTATION_ALLOWED)
        connection.initializingCall()
        return connection
    }
}
class ZoomVoIPConnection(
    private val zoomViewModel: ZoomSessionViewModel
) : Connection() {
    fun initializingCall() {
        connectionCapabilities = CAPABILITY_MUTE or CAPABILITY_SUPPORT_HOLD or CAPABILITY_HOLD
        audioModeIsVoip = true
        setInitializing()
    }

    fun disconnect() {
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
        zoomViewModel.closeSession(end = true)
    }

    override fun onAnswer() {
        super.onAnswer()
        setActive()
        zoomViewModel.joinSession(this)
    }

    override fun onReject() {
        super.onReject()
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        destroy()
    }

    override fun onDisconnect() {
        super.onDisconnect()
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
        zoomViewModel.closeSession(end = true)
    }

    override fun onMuteStateChanged(isMuted: Boolean) {
        super.onMuteStateChanged(isMuted)
        zoomViewModel.toggleMicrophone()
    }
}