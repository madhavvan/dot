package com.example.Autply

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MyAccessibilityService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("MyAccessibilityService", "Service instance created")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d("MyAccessibilityService", "Service instance destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName != "com.whatsapp") return

        Log.d("MyAccessibilityService", "Event received: ${event.eventType}")

        val rootNode = rootInActiveWindow ?: return
        processNotification(rootNode)
    }

    override fun onInterrupt() {
        Log.d("MyAccessibilityService", "Service interrupted")
    }

    fun sendReplyToWhatsApp(reply: String) {
        val rootNode = rootInActiveWindow ?: run {
            Log.d("MyAccessibilityService", "Root node is null")
            return
        }

        // Find the input field for the reply box
        val inputFields = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/entry")
        if (inputFields.isNullOrEmpty()) {
            Log.d("MyAccessibilityService", "Reply box not found")
            return
        }

        // Set the text in the reply box
        val inputField = inputFields[0]
        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, reply)
        }
        inputField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        Log.d("MyAccessibilityService", "Text set in reply box: $reply")

        // Find and click the Send button
        val sendButtons = rootNode.findAccessibilityNodeInfosByViewId("com.whatsapp:id/send")
        if (!sendButtons.isNullOrEmpty()) {
            sendButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Log.d("MyAccessibilityService", "Send button clicked")
        } else {
            Log.d("MyAccessibilityService", "Send button not found")
        }
    }

    private fun processNotification(rootNode: AccessibilityNodeInfo) {
        Log.d("MyAccessibilityService", "Processing notification...")
        // Future implementation for handling other notifications
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
        Log.d("MyAccessibilityService", "Service connected")
    }
}
