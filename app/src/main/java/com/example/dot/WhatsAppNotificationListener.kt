package com.example.Autply

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log // Importing Log for logging messages

class WhatsAppNotificationListener : NotificationListenerService() {

    private val huggingFaceHandler by lazy { HuggingFaceHandler() }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn?.notification?.let {
            val packageName = sbn.packageName
            val extras = it.extras
            val sender = extras.getString("android.title") ?: "Unknown sender"
            val message = extras.getString("android.text") ?: "Content hidden"

            if (packageName == "com.whatsapp") {
                Log.d("WhatsAppNotification", "Message received from $sender: $message")
                // Generate AI-suggested reply
                huggingFaceHandler.generateReply(message) { suggestedReply ->
                    showPopupNotification(baseContext, sender, suggestedReply)
                }
            }
        }
    }

    private fun showPopupNotification(context: Context, sender: String, suggestedReply: String) {
        Handler(Looper.getMainLooper()).post {
            val editText = android.widget.EditText(context).apply {
                setText(suggestedReply)
            }

            AlertDialog.Builder(context)
                .setTitle("New Message from $sender")
                .setView(editText)
                .setPositiveButton("Send") { dialog, _ ->
                    val replyText = editText.text.toString()
                    sendReplyUsingAccessibilityService(replyText)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .create()
                .apply {
                    window?.setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
                }
                .show()
        }
    }

    private fun sendReplyUsingAccessibilityService(reply: String) {
        MyAccessibilityService.instance?.sendReplyToWhatsApp(reply)
            ?: Log.d("WhatsAppNotificationListener", "Accessibility service instance is null")
    }
}
