package com.wc.clipper

import android.app.Activity
import android.content.*
import android.util.Log


class ClipperReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isActionSet(intent.action)) {
            Log.d(TAG, "Setting text into clipboard")
            val text = intent.getStringExtra(EXTRA_TEXT)
            if (text.isNullOrEmpty()) {
                resultCode = Activity.RESULT_CANCELED
                resultData = "No text is provided. Use -e text \"text to be pasted\""
            } else {
                copyStr(context, text)
                resultCode = Activity.RESULT_OK
                resultData = "Text is copied into clipboard."
            }
        } else if (isActionGet(intent.action)) {
            Log.d(TAG, "Getting text from clipboard")
            val text = getClipStr(context)
            Log.d(TAG, String.format("Clipboard text: %s", text))
            resultCode = Activity.RESULT_OK
            resultData = text
        }
    }

    private fun getClipStr(context: Context): String {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.primaryClip?.let {
            val text = it.getItemAt(0).text
            if (text.isNullOrEmpty().not()) {
                return text.toString()
            }
        }
        return ""
    }

    private fun copyStr(context: Context, msg: String?) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
            val mClipData = ClipData.newPlainText("Label", msg)
            try {
                it.setPrimaryClip(mClipData)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val TAG = "ClipboardReceiver"
        var ACTION_GET = "clipper.get"
        var ACTION_SET = "clipper.set"
        var EXTRA_TEXT = "text"
        fun isActionGet(action: String?): Boolean {
            return ACTION_GET == action
        }

        fun isActionSet(action: String?): Boolean {
            return ACTION_SET == action
        }
    }
}