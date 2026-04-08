package com.ashu.callapitestcode.other.CustomDialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.ashu.callapitestcode.R
import com.ashu.callapitestcode.uitils.NetworkUtils
import com.google.android.material.progressindicator.LinearProgressIndicator

class AshDialog(
    private val context: Context,
    title: String = "Please wait",
    message: String = "Loading…"
) {

    private var progress: Dialog? = null
    private var iconImageView: ImageView? = null
    private var titleTextView: TextView? = null
    private var messageTextView: TextView? = null
    private var progressIndicator: LinearProgressIndicator? = null

    private var showTimestamp = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var progressValue = 0

    private val loadingTexts = arrayOf(
        "Loading…",
        "Please wait…",
        "Fetching data…",
        "Almost done…"
    )
    private var loadingIndex = 0
    private var textAnimator: Runnable? = null

    init {
        dialogBox(title, message)
    }

    fun setTitle(title: String) {
        titleTextView?.text = title
    }

    fun setMessage(message: String) {
        messageTextView?.text = message
    }

    private fun dialogBox(title: String, message: String) {
        try {
            progress = Dialog(context).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setContentView(R.layout.custom_dialog_layout)
                setCancelable(false)

                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            iconImageView = progress?.findViewById(R.id.iconImageView)
            titleTextView = progress?.findViewById(R.id.titleTextView)
            messageTextView = progress?.findViewById(R.id.messageTextView)
            progressIndicator = progress?.findViewById(R.id.progressIndicator)

            titleTextView?.text = title
            messageTextView?.text = message

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startProgressAnimation() {
        progressValue = 0

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (progress == null || !progress!!.isShowing) return

                progressValue += 5
                progressIndicator?.progress = progressValue

                if (progressValue < 100) {
                    handler.postDelayed(this, 80)
                }
            }
        }, 80)
    }

    fun show() {
        val dialog = progress ?: return
        if (context !is Activity) return

        val activity = context
        if (activity.isFinishing || activity.isDestroyed) return

        if (!NetworkUtils.isInternetAvailable(context)) {
            Toast.makeText(context, "Internet may not be available", Toast.LENGTH_SHORT).show()
            return
        }

        if (!dialog.isShowing) {
            dialog.show()
            showTimestamp = System.currentTimeMillis()
            startLoadingTextAnimation()
            startProgressAnimation()
        }
    }

    fun dismiss() {
        val dialog = progress ?: return

        handler.removeCallbacksAndMessages(null)

        val elapsed = System.currentTimeMillis() - showTimestamp
        val delay = maxOf(0, 800 - elapsed)

        handler.postDelayed({
            try {
                if (dialog.isShowing) {
                    if (context is Activity) {
                        val activity = context
                        if (activity.isFinishing || activity.isDestroyed) return@postDelayed
                    }
                    dialog.dismiss()
                }
            } catch (_: Exception) {
            }
        }, delay)
    }

    private fun startLoadingTextAnimation() {
        textAnimator = object : Runnable {
            override fun run() {
                if (progress != null && progress!!.isShowing) {
                    messageTextView?.text = loadingTexts[loadingIndex]
                    loadingIndex = (loadingIndex + 1) % loadingTexts.size
                    handler.postDelayed(this, 1200)
                }
            }
        }
        handler.post(textAnimator!!)
    }

    fun isVisible(): Boolean {
        return progress?.isShowing == true
    }
}