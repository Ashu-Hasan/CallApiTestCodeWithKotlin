package com.ashu.callapitestcode.other.graphs;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ashu.callapitestcode.data.model.WeatherItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * WeatherGraphView
 *
 * Custom View to display:
 * 1. Time (e.g., 4PM, 5PM)
 * 2. Weather Icon (SVG/Image)
 * 3. Temperature (with °)
 * 4. Smooth Graph Line
 * 5. Rain Percentage
 *
 * Everything is perfectly aligned vertically & scrollable horizontally
 */
public class WeatherGraphView extends View {

    // =========================
    // DATA SOURCE
    // =========================

    /**
     * Main data list
     * Example:
     * time = "4PM"
     * iconUrl = "https://..."
     * temp = 17
     * rain = 20
     */
    private List<WeatherItem> data = new ArrayList<>();

    /**
     * Bitmap icons loaded from URL
     * (same index as data list)
     */
    private List<Bitmap> icons = new ArrayList<>();


    // =========================
    // CUSTOMIZATION VARIABLES
    // =========================

    // 🔹 Spacing between each item (controls horizontal scroll width)
    private float itemWidth = 180f;

    // 🔹 Vertical positions (adjust layout spacing)
    private float timeY = 40f;        // Time position (top)
    private float iconY = 75f;       // Icon position
    private float tempY = 200f;       // Temperature position
    private float graphTop = 240f;    // Graph start Y
    private float graphHeight = 60f; // Graph height
    private float rainY = 360f;       // Rain % position

    // 🔹 Icon size
    private int iconSize = 60;


    // =========================
    // PAINT OBJECTS (STYLING)
    // =========================

    private Paint linePaint;      // Graph line
    private Paint dotPaint;       // Dot on graph
    private Paint tempPaint;      // Temperature text
    private Paint timePaint;      // Time text
    private Paint rainPaint;      // Rain text

    private Path path;


    public WeatherGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Initialize paints and styles
     */
    private void init() {

        // Graph line
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);

        // Dot indicator
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.WHITE);

        // Temperature text
        tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempPaint.setColor(Color.WHITE);
        tempPaint.setTextSize(40f); // 🔥 Customize size
        tempPaint.setTextAlign(Paint.Align.CENTER);

        // Time text
        timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        timePaint.setColor(Color.LTGRAY);
        timePaint.setTextSize(26f);
        timePaint.setTextAlign(Paint.Align.CENTER);

        // Rain text
        rainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rainPaint.setColor(Color.LTGRAY);
        rainPaint.setTextSize(26f);
        rainPaint.setTextAlign(Paint.Align.CENTER);

        path = new Path();
    }


    // =========================
    // PUBLIC METHODS
    // =========================

    /**
     * Set data to graph
     */
    public void setData(List<WeatherItem> list) {
        this.data = list;
        loadIcons();
        requestLayout(); // adjust width
        invalidate();    // redraw
    }

    /**
     * Customize text size
     */
    public void setTextSizes(float tempSize, float timeSize, float rainSize) {
        tempPaint.setTextSize(tempSize);
        timePaint.setTextSize(timeSize);
        rainPaint.setTextSize(rainSize);
        invalidate();
    }

    /**
     * Customize colors
     */
    public void setColors(int lineColor, int textColor) {
        linePaint.setColor(lineColor);
        tempPaint.setColor(textColor);
        timePaint.setColor(textColor);
        rainPaint.setColor(textColor);
        dotPaint.setColor(textColor);
        invalidate();
    }

    /**
     * Customize spacing (vertical layout)
     */
    public void setVerticalSpacing(float time, float icon, float temp, float graph, float rain) {
        timeY = time;
        iconY = icon;
        tempY = temp;
        graphTop = graph;
        rainY = rain;
        invalidate();
    }

    /**
     * Customize horizontal spacing
     */
    public void setItemWidth(float width) {
        this.itemWidth = width;
        requestLayout();
    }


    // =========================
    // ICON LOADING (Coil/Custom)
    // =========================

    private void loadIcons() {

        // Create fixed size list to avoid mismatch
        icons = new ArrayList<>(Collections.nCopies(data.size(), null));

        for (int i = 0; i < data.size(); i++) {

            int index = i;

            // Replace with your Coil method
            ImageLoaderUtil.loadSvgBitmap(getContext(), data.get(i).iconUrl, bitmap -> {

                if (bitmap != null) {
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, iconSize, iconSize, true);
                    icons.set(index, scaled);
                    invalidate();
                }
            });
        }
    }


    // =========================
    // VIEW SIZE (SCROLL SUPPORT)
    // =========================

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (itemWidth * data.size());
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }


    // =========================
    // DRAWING LOGIC
    // =========================

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty()) return;

        float max = getMax();
        float min = getMin();

        float range = (max - min == 0) ? 1 : (max - min);

        path.reset();

        float prevX = 0, prevY = 0;

        for (int i = 0; i < data.size(); i++) {

            float x = i * itemWidth + itemWidth / 2;

            float y = graphTop + (1 - (data.get(i).temp - min) / range) * graphHeight;

            // Smooth curve
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                float midX = (prevX + x) / 2;
                path.cubicTo(midX, prevY, midX, y, x, y);
            }

            prevX = x;
            prevY = y;

            // Draw TIME
            canvas.drawText(data.get(i).time, x, timeY, timePaint);

            // Draw ICON
            if (i < icons.size() && icons.get(i) != null) {
                canvas.drawBitmap(icons.get(i), x - iconSize / 2, iconY, null);
            }

            // Draw TEMP
            canvas.drawText((int) data.get(i).temp + "°", x, tempY, tempPaint);

            // Draw DOT
            canvas.drawCircle(x, y, 6, dotPaint);

            // Draw RAIN %
            canvas.drawText("☁ " + data.get(i).rain + "%", x, rainY, rainPaint);
        }

        canvas.drawPath(path, linePaint);
    }


    // =========================
    // HELPER METHODS
    // =========================

    private float getMax() {
        float max = data.get(0).temp;
        for (WeatherItem item : data) if (item.temp > max) max = item.temp;
        return max;
    }

    private float getMin() {
        float min = data.get(0).temp;
        for (WeatherItem item : data) if (item.temp < min) min = item.temp;
        return min;
    }

    public void adjustLayout(float iconGap, float tempGap, float graphSize) {
        iconY = timeY + iconGap;
        tempY = iconY + tempGap;
        graphHeight = graphSize;
        invalidate();
    }
}

/*
public class WeatherGraphView extends View {

    private List<WeatherItem> data = new ArrayList<>();
    private List<Bitmap> icons = new ArrayList<>();

    private Paint linePaint, dotPaint, tempPaint, subTextPaint;
    private Path path;

    private float itemWidth = 140f; // spacing for scroll

    public WeatherGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(Color.WHITE);

        tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempPaint.setColor(Color.WHITE);
        tempPaint.setTextSize(40f); // BIG TEMP TEXT
        tempPaint.setTextAlign(Paint.Align.CENTER);

        subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTextPaint.setColor(Color.LTGRAY);
        subTextPaint.setTextSize(26f);
        subTextPaint.setTextAlign(Paint.Align.CENTER);

        path = new Path();
    }

    public void setData(List<WeatherItem> list) {
        this.data = list;
        loadIcons();
        requestLayout();
        invalidate();
    }

    private void loadIcons() {

        icons = new ArrayList<>(Collections.nCopies(data.size(), null));

        for (int i = 0; i < data.size(); i++) {

            int index = i;

            loadSvgBitmap(getContext(), data.get(i).iconUrl, bitmap -> {

                if (bitmap != null) {
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
                    icons.set(index, scaled);
                    invalidate();
                }

            });
        }
    }

    public interface SvgCallback {
        void onLoaded(Bitmap bitmap);
    }
    public static void loadSvgBitmap(Context context, String url, SvgCallback callback) {

        ImageLoader loader = new ImageLoader.Builder(context)
                .components(new ComponentRegistry.Builder()
                        .add(new SvgDecoder.Factory())
                        .build())
                .build();

        ImageRequest request = new ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .target(new Target() {
                    @Override
                    public void onStart(@Nullable Drawable placeholder) {}

                    @Override
                    public void onError(@Nullable Drawable error) {}

                    @Override
                    public void onSuccess(@NonNull Drawable result) {
                        Bitmap bitmap = drawableToBitmap(result);
                        callback.onLoaded(bitmap);
                    }
                })
                .build();

        loader.enqueue(request);
    }

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (itemWidth * data.size());
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty()) return;

        float height = getHeight();

        float topTime = 40;
        float iconY = 100;
        float tempY = 170;
        float graphTop = 220;
        float graphHeight = 200;
        float rainY = graphTop + graphHeight + 60;

        float max = getMax();
        float min = getMin();

        path.reset();

        float prevX = 0, prevY = 0;

        for (int i = 0; i < data.size(); i++) {

            float x = i * itemWidth + itemWidth / 2;

            float y = graphTop + (1 - (data.get(i).temp - min) / (max - min)) * graphHeight;

            // Smooth curve (Bezier)
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                float midX = (prevX + x) / 2;
                path.cubicTo(midX, prevY, midX, y, x, y);
            }

            prevX = x;
            prevY = y;

            // TIME
            canvas.drawText(data.get(i).time, x, topTime, subTextPaint);

            // ICON
            if (i < icons.size() && icons.get(i) != null && !icons.get(i).isRecycled()) {
                canvas.drawBitmap(icons.get(i), x - 30, iconY, null);
            }

            // TEMP
            canvas.drawText((int) data.get(i).temp + "°", x, tempY, tempPaint);

            // DOT
            canvas.drawCircle(x, y, 6, dotPaint);

            // RAIN %
            canvas.drawText("☁ " + data.get(i).rain + "%", x, rainY, subTextPaint);
        }

        canvas.drawPath(path, linePaint);
    }

    private float getMax() {
        float max = data.get(0).temp;
        for (WeatherItem item : data) if (item.temp > max) max = item.temp;
        return max;
    }

    private float getMin() {
        float min = data.get(0).temp;
        for (WeatherItem item : data) if (item.temp < min) min = item.temp;
        return min;
    }
}
*/
