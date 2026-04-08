package com.ashu.callapitestcode.other.graphs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.target.Target

object ImageLoaderUtil {

    // 🔥 Singleton ImageLoader
    private var imageLoader: ImageLoader? = null

    private fun getImageLoader(context: Context): ImageLoader {
        return imageLoader ?: ImageLoader.Builder(context.applicationContext)
            .components(
                ComponentRegistry.Builder()
                    .add(SvgDecoder.Factory())
                    .build()
            )
            .build()
            .also { imageLoader = it }
    }

    // =========================================================
    // ✅ 1. LOAD SVG → BITMAP
    // =========================================================
    fun loadSvgBitmap(
        context: Context,
        url: String,
        callback: SvgCallback
    ) {

        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // required for bitmap
            .target(object : Target {

                override fun onStart(placeholder: Drawable?) {}

                override fun onError(error: Drawable?) {
                    callback.onLoaded(null)
                }

                override fun onSuccess(result: Drawable) {
                    val bitmap = drawableToBitmap(result)
                    callback.onLoaded(bitmap)
                }
            })
            .build()

        getImageLoader(context).enqueue(request)
    }

    // =========================================================
    // ✅ 2. LOAD SVG → IMAGEVIEW
    // =========================================================
    fun loadSvgIntoImageView(
        context: Context,
        url: String,
        imageView: ImageView
    ) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .target(imageView)
            .build()

        getImageLoader(context).enqueue(request)
    }

    // =========================================================
    // ✅ 3. LOAD NORMAL IMAGE
    // =========================================================
    fun loadImage(
        context: Context,
        url: String,
        imageView: ImageView
    ) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .target(imageView)
            .build()

        getImageLoader(context).enqueue(request)
    }

    // =========================================================
    // 🔧 Drawable → Bitmap
    // =========================================================
    fun drawableToBitmap(drawable: Drawable): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 100
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 100

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    // =========================================================
    // CALLBACK
    // =========================================================
    fun interface SvgCallback {
        fun onLoaded(bitmap: Bitmap?)
    }
}