package com.ashu.callapitestcode.other.graphs;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class AQISeekBar extends View {

    private Paint paint, textPaint;

    private boolean isUserInteractionEnabled = true;

    private float progress = 100;
    private float max = 600;
    private float thumbStrokeWidth = 6f;
    public static final int TEXT_GRAVITY_START = 0;
    public static final int TEXT_GRAVITY_END = 1;
    float lastDrawnRight = -Float.MAX_VALUE;

    private int bottomTextGravity = TEXT_GRAVITY_START;

    private float thumbRadius = 22f;
    private float trackHeight = 14f;
    private float padding = 100f;

    private boolean showLabels = true;
    private boolean showBubble = true;

    private Drawable customThumbDrawable = null;

    // 🔽 Bottom Mode System
    public static final int BOTTOM_NONE = 0;
    public static final int BOTTOM_NUMBERS = 1;
    public static final int BOTTOM_CUSTOM = 2;

    private int bottomMode = BOTTOM_NUMBERS;
    private List<BottomText> bottomTextList = new ArrayList<>();
    private List<String> customBottomTexts = new ArrayList<>();
    public static class BottomText {
        public String startText; // left
        public String endText;   // right

        public BottomText(String startText, String endText) {
            this.startText = startText;
            this.endText = endText;
        }
    }

    // 🎯 Level class
    public static class Level {
        public float min, max;
        public int color;
        public String label;

        public Level(float min, float max, int color, String label) {
            this.min = min;
            this.max = max;
            this.color = color;
            this.label = label;
        }
    }

    private List<Level> levels = new ArrayList<>();

    public AQISeekBar(Context context) {
        super(context);
        init();
    }

    public AQISeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // shadow support
        setLayerType(LAYER_TYPE_SOFTWARE, paint);

        // default levels
        levels.add(new Level(0, 50, Color.GREEN, "Good"));
        levels.add(new Level(50, 100, Color.YELLOW, "Moderate"));
        levels.add(new Level(100, 200, Color.parseColor("#FFA500"), "Poor"));
        levels.add(new Level(200, 300, Color.MAGENTA, "Unhealthy"));
        levels.add(new Level(300, 400, Color.parseColor("#FF6666"), "Severe"));
        levels.add(new Level(400, 500, Color.RED, "Hazardous"));
    }


    //    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        float width = getWidth();
//        float centerY = getHeight() / 2f;
//
//        float startX = padding;
//        float endX = width - padding;
//        float totalWidth = endX - startX;
//
//        int levelCount = levels.size();
//        float segmentWidth = totalWidth / levelCount;
//
//        // 🎨 segments
//        for (Level level : levels) {
//            float left = startX + (level.min / max) * totalWidth;
//            float right = startX + (level.max / max) * totalWidth;
//
//            paint.setColor(level.color);
//            paint.setStrokeWidth(trackHeight);
//            paint.setStrokeCap(Paint.Cap.ROUND);
//
//            canvas.drawLine(left, centerY, right, centerY, paint);
//        }
//
//        float ratio = progress / max;
//        float progressX = startX + (ratio * totalWidth);
//        int currentColor = getColorForProgress(progress);
//
//        // 🎯 Thumb / Icon
//        if (customThumbDrawable != null) {
//            int size = 60;
//            customThumbDrawable.setBounds(
//                    (int) (progressX - size / 2),
//                    (int) (centerY - size / 2),
//                    (int) (progressX + size / 2),
//                    (int) (centerY + size / 2)
//            );
//            customThumbDrawable.draw(canvas);
//        } else {
//            paint.setShadowLayer(8f, 0, 2f, Color.GRAY);
//
//            // 🔵 Border
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setStrokeWidth(thumbStrokeWidth);
//            paint.setColor(currentColor);
//            canvas.drawCircle(progressX, centerY, thumbRadius, paint);
//
//            // ⚪ Center
//            paint.setStyle(Paint.Style.FILL);
//            paint.setColor(Color.WHITE);
//            canvas.drawCircle(progressX, centerY, thumbRadius - thumbStrokeWidth, paint);
//
//            paint.clearShadowLayer();
//        }
//
//        // 🔤 Labels
//        if (showLabels) {
//            for (Level level : levels) {
//                float mid = (level.min + level.max) / 2f;
//                float x = startX + (mid / max) * totalWidth;
//                canvas.drawText(level.label, x, centerY - 60, textPaint);
//            }
//        }
//
//        // 🔢 Bottom Mode System
//       /* if (bottomMode != BOTTOM_NONE) {
//
//            if (bottomMode == BOTTOM_NUMBERS) {
//
//                for (Level level : levels) {
//                    float x = startX + (level.min / max) * totalWidth;
//                    canvas.drawText(String.valueOf((int) level.min), x, centerY + 80, textPaint);
//                }
//
//                canvas.drawText(String.valueOf((int) max),
//                        startX + totalWidth,
//                        centerY + 80,
//                        textPaint);
//
//            } else if (bottomMode == BOTTOM_CUSTOM) {
//
//                int size = Math.min(customBottomTexts.size(), levels.size());
//
//                for (int i = 0; i < size; i++) {
//                    Level level = levels.get(i);
//                    float value;
//                    if (bottomTextGravity == TEXT_GRAVITY_END) {
//                        value = level.max;
//                    } else {
//                        value = level.min;
//                    }
//                    float x = startX + (value / max) * totalWidth;
//                    canvas.drawText(customBottomTexts.get(i), x, centerY + 80, textPaint);
//                }
//
//                if (customBottomTexts.size() > levels.size()) {
//                    canvas.drawText(
//                            customBottomTexts.get(customBottomTexts.size() - 1),
//                            startX + totalWidth,
//                            centerY + 80,
//                            textPaint
//                    );
//                }
//            }
//        }*/
//        // 🔢 Bottom Mode System
//        if (bottomMode != BOTTOM_NONE) {
//
//            if (bottomMode == BOTTOM_NUMBERS) {
//
//                float lastDrawnRight = -Float.MAX_VALUE;
//
//                for (Level level : levels) {
//
//                    String text = String.valueOf((int) level.min);
//                    float x = startX + (level.min / max) * totalWidth;
//
//                    float textWidth = textPaint.measureText(text);
//                    float left = x - textWidth / 2;
//                    float right = x + textWidth / 2;
//
//                    if (left < lastDrawnRight + 10) continue;
//
//                    canvas.drawText(text, x, centerY + 80, textPaint);
//                    lastDrawnRight = right;
//                }
//
//                // last max
//                String maxText = String.valueOf((int) max);
//                float x = startX + totalWidth;
//
//                float textWidth = textPaint.measureText(maxText);
//                float left = x - textWidth / 2;
//
//                if (left >= lastDrawnRight + 10) {
//                    canvas.drawText(maxText, x, centerY + 80, textPaint);
//                }
//
//            } else if (bottomMode == BOTTOM_CUSTOM) {
//
//                int size = Math.min(customBottomTexts.size(), levels.size());
//
//                float lastDrawnRight = -Float.MAX_VALUE;
//
//                for (int i = 0; i < size; i++) {
//
//                    String text = customBottomTexts.get(i);
//                    Level level = levels.get(i);
//
//                    float value = (bottomTextGravity == TEXT_GRAVITY_END)
//                            ? level.max
//                            : level.min;
//
//                    float x = startX + (value / max) * totalWidth;
//
//                    float textWidth = textPaint.measureText(text);
//                    float left = x - textWidth / 2;
//                    float right = x + textWidth / 2;
//
//                    if (left < lastDrawnRight + 10) continue;
//
//                    canvas.drawText(text, x, centerY + 80, textPaint);
//                    lastDrawnRight = right;
//                }
//
//                // last extra value
//                if (customBottomTexts.size() > levels.size()) {
//
//                    String text = customBottomTexts.get(customBottomTexts.size() - 1);
//                    float x = startX + totalWidth;
//
//                    float textWidth = textPaint.measureText(text);
//                    float left = x - textWidth / 2;
//
//                    if (left >= lastDrawnRight + 10) {
//                        canvas.drawText(text, x, centerY + 80, textPaint);
//                    }
//                }
//            }
//        }
//
//        // 💬 Bubble
//        if (showBubble) {
//            drawBubble(canvas, progressX, centerY, currentColor);
//        }
//    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float centerY = getHeight() / 2f;

        // ✅ safe padding (thumb cut na ho)
        float safePadding = Math.max(padding, thumbRadius + 20);

        float startX = safePadding;
        float endX = width - safePadding;
        float totalWidth = endX - startX;

        int levelCount = levels.size();
        float segmentWidth = totalWidth / levelCount;

        // 🎨 Equal segments
        for (int i = 0; i < levelCount; i++) {

            Level level = levels.get(i);

            float left = startX + (i * segmentWidth);
            float right = left + segmentWidth;

            paint.setColor(level.color);
            paint.setStrokeWidth(trackHeight);

            // rounded only edges
            if (i == 0 || i == levelCount - 1) {
                paint.setStrokeCap(Paint.Cap.ROUND);
            } else {
                paint.setStrokeCap(Paint.Cap.BUTT);
            }

            canvas.drawLine(left, centerY, right, centerY, paint);
        }

        // 📍 Progress
        float ratio = progress / max;
        float progressX = startX + (ratio * totalWidth);

        int currentColor = getColorForProgress(progress);

        // 🎯 Thumb / Icon
        if (customThumbDrawable != null) {
            int size = 60;
            customThumbDrawable.setBounds(
                    (int) (progressX - size / 2),
                    (int) (centerY - size / 2),
                    (int) (progressX + size / 2),
                    (int) (centerY + size / 2)
            );
            customThumbDrawable.draw(canvas);
        } else {
            paint.setShadowLayer(8f, 0, 2f, Color.GRAY);

            // 🔵 Border
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(thumbStrokeWidth);
            paint.setColor(currentColor);
            canvas.drawCircle(progressX, centerY, thumbRadius, paint);

            // ⚪ Center
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(progressX, centerY, thumbRadius - thumbStrokeWidth, paint);

            paint.clearShadowLayer();
        }

        // 🔤 Labels (equal spacing center)
        if (showLabels) {
            for (int i = 0; i < levelCount; i++) {
                Level level = levels.get(i);

                float x = startX + (i * segmentWidth) + (segmentWidth / 2);

                canvas.drawText(level.label, x, centerY - 60, textPaint);
            }
        }

        // 🔢 Bottom Mode System (NO OVERLAP + EQUAL SPACING)
        if (bottomMode != BOTTOM_NONE) {

            float lastDrawnRight = -Float.MAX_VALUE;

            for (int i = 0; i < levelCount; i++) {

                String text;

                if (bottomMode == BOTTOM_NUMBERS) {
                    text = String.valueOf((int) levels.get(i).min);
                } else {
                    if (i >= customBottomTexts.size()) break;
                    text = customBottomTexts.get(i);
                }

                float x;

                if (bottomTextGravity == TEXT_GRAVITY_END) {
                    x = startX + ((i + 1) * segmentWidth);
                } else {
                    x = startX + (i * segmentWidth);
                }

                float textWidth = textPaint.measureText(text);
                float left = x - textWidth / 2;
                float right = x + textWidth / 2;

                // 🚫 overlap avoid
                if (left < lastDrawnRight + 10) continue;

                canvas.drawText(text, x, centerY + 80, textPaint);
                lastDrawnRight = right;
            }

            // last max value (numbers only)
            if (bottomMode == BOTTOM_NUMBERS) {

                String text = String.valueOf((int) max);
                float x = startX + totalWidth;

                float textWidth = textPaint.measureText(text);
                float left = x - textWidth / 2;

                if (left >= lastDrawnRight + 10) {
                    canvas.drawText(text, x, centerY + 80, textPaint);
                }
            }
        }

        // 💬 Bubble
        if (showBubble) {
            drawBubble(canvas, progressX, centerY, currentColor);
        }
        //BOTTOM_CUSTOM
        if (bottomMode == BOTTOM_CUSTOM) {

            float lastDrawnRight = -Float.MAX_VALUE;

            for (int i = 0; i < levels.size(); i++) {

                if (i >= bottomTextList.size()) break;

                BottomText bt = bottomTextList.get(i);

                // 🔹 START TEXT
                if (bt.startText != null) {

                    float x = startX + (i * segmentWidth);

                    float textWidth = textPaint.measureText(bt.startText);
                    float left = x - textWidth / 2;
                    float right = x + textWidth / 2;

                    if (left >= lastDrawnRight + 10) {
                        canvas.drawText(bt.startText, x, centerY + 80, textPaint);
                        lastDrawnRight = right;
                    }
                }

                // 🔹 END TEXT
                if (bt.endText != null) {

                    float x = startX + ((i + 1) * segmentWidth);

                    float textWidth = textPaint.measureText(bt.endText);
                    float left = x - textWidth / 2;
                    float right = x + textWidth / 2;

                    if (left >= lastDrawnRight + 10) {
                        canvas.drawText(bt.endText, x, centerY + 80, textPaint);
                        lastDrawnRight = right;
                    }
                }
            }
        }
    }
    private void drawBubble(Canvas canvas, float x, float centerY, int color) {
        String text = (int) progress + " - " + getLabelForProgress(progress);

        textPaint.setTextSize(30f);

        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);

        float padding = 20f;
        float w = bounds.width() + padding * 2;
        float h = bounds.height() + padding;

        float left = x - w / 2;
        float top = centerY - 150;

        paint.setColor(color);
        canvas.drawRoundRect(left, top, left + w, top + h, 20f, 20f, paint);

        textPaint.setColor(Color.WHITE);
        canvas.drawText(text, x, top + h - 20, textPaint);

        textPaint.setColor(Color.BLACK);
    }

    private int getColorForProgress(float progress) {
        for (Level level : levels) {
            if (progress >= level.min && progress <= level.max) {
                return level.color;
            }
        }
        return Color.GRAY;
    }

    private String getLabelForProgress(float progress) {
        for (Level level : levels) {
            if (progress >= level.min && progress <= level.max) {
                return level.label;
            }
        }
        return "";
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        // ❌ User interaction disabled
        if (!isUserInteractionEnabled) {
            return false;
        }

        float width = getWidth();
        float startX = padding;
        float endX = width - padding;

        float x = event.getX();

        if (x < startX) x = startX;
        if (x > endX) x = endX;

        float totalWidth = endX - startX;
        progress = ((x - startX) / totalWidth) * max;

        invalidate();
        return true;

    }

    // =========================
    // 🔧 PUBLIC METHODS
    // =========================

    public void setLevels(List<Level> levels) {
        this.levels = levels;
        invalidate();
    }

    public void setShowLabels(boolean show) {
        this.showLabels = show;
        invalidate();
    }

    public void setShowBubble(boolean show) {
        this.showBubble = show;
        invalidate();
    }

    public void setCustomThumbDrawable(Drawable drawable) {
        this.customThumbDrawable = drawable;
        invalidate();
    }

    public void setBottomMode(int mode) {
        this.bottomMode = mode;
        invalidate();
    }

    public void setCustomBottomTexts(List<String> texts) {
        this.customBottomTexts = texts;
        invalidate();
    }

    public void setProgress(float progress) {
        // limit within range
        if (progress < 0) progress = 0;
        if (progress > max) progress = max;

        this.progress = progress;
        invalidate();
    }

    public void setUserInteractionEnabled(boolean enabled) {
        this.isUserInteractionEnabled = enabled;
    }

    public float getProgress() {
        return progress;
    }

    public void setThumbRadius(float radius) {
        this.thumbRadius = radius;
        invalidate();
    }

    public void setThumbStrokeWidth(float width) {
        this.thumbStrokeWidth = Math.min(width, thumbRadius - 2);
        invalidate();
    }
    public void setBottomTexts(List<BottomText> list) {
        this.bottomTextList = list;
        invalidate();
    }

    public void setBottomTextGravity(int gravity) {
        this.bottomTextGravity = gravity;
        invalidate();
    }
}