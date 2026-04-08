package com.ashu.callapitestcode.other.graphs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import coil.ComponentRegistry;
import coil.ImageLoader;
import coil.decode.SvgDecoder;
import coil.request.ImageRequest;
import coil.target.Target;

public class ImageLoaderUtil {

    // 🔥 Singleton ImageLoader (important for performance)
    private static ImageLoader imageLoader;

    private static ImageLoader getImageLoader(Context context) {
        if (imageLoader == null) {
            imageLoader = new ImageLoader.Builder(context.getApplicationContext())
                    .components(new ComponentRegistry.Builder()
                            .add(new SvgDecoder.Factory())
                            .build())
                    .build();
        }
        return imageLoader;
    }

    // =========================================================
    // ✅ 1. LOAD SVG → BITMAP (For Custom View like Graph)
    // =========================================================
    public static void loadSvgBitmap(Context context, String url, SvgCallback callback) {

        ImageRequest request = new ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // required for bitmap
                .target(new Target() {
                    @Override
                    public void onStart(@Nullable Drawable placeholder) {}

                    @Override
                    public void onError(@Nullable Drawable error) {
                        callback.onLoaded(null);
                    }

                    @Override
                    public void onSuccess(@NonNull Drawable result) {
                        Bitmap bitmap = drawableToBitmap(result);
                        callback.onLoaded(bitmap);
                    }
                })
                .build();

        getImageLoader(context).enqueue(request);
    }

    // =========================================================
    // ✅ 2. LOAD SVG → IMAGEVIEW (NEW METHOD 🔥)
    // =========================================================
    public static void loadSvgIntoImageView(Context context, String url, ImageView imageView) {

        ImageRequest request = new ImageRequest.Builder(context)
                .data(url)
                .target(imageView) // direct load into ImageView
                .build();

        getImageLoader(context).enqueue(request);
    }

    // =========================================================
    // ✅ 3. LOAD NORMAL IMAGE (JPG/PNG)
    // =========================================================
    public static void loadImage(Context context, String url, ImageView imageView) {

        ImageRequest request = new ImageRequest.Builder(context)
                .data(url)
                .target(imageView)
                .build();

        getImageLoader(context).enqueue(request);
    }

    // =========================================================
    // 🔧 Drawable → Bitmap Converter
    // =========================================================
    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 100;
        int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 100;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    // =========================================================
    // CALLBACK
    // =========================================================
    public interface SvgCallback {
        void onLoaded(Bitmap bitmap);
    }
}