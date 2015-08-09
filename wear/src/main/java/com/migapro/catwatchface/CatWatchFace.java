package com.migapro.catwatchface;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import java.util.Calendar;

public class CatWatchFace {

    private static final String TIME_FORMAT = "%02d:%02d";

    private Calendar mCalendar;

    private Paint mTimePaint;
    private Bitmap mBackgroundBitmap;
    private Bitmap mBackgroundScaledBitmap;

    public static CatWatchFace newInstance(Context context) {
        Resources resources = context.getResources();

        Bitmap backgroundBitmap = ((BitmapDrawable)resources.getDrawable(R.drawable.cat4, null)).getBitmap();

        Paint timePaint = new Paint();
        timePaint.setColor(Color.WHITE);
        timePaint.setTextSize(resources.getDimension(R.dimen.paint_time_text));
        timePaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        return new CatWatchFace(timePaint, backgroundBitmap, calendar);
    }

    private CatWatchFace(Paint timePaint, Bitmap backgroundBitmap, Calendar calendar) {
        mTimePaint = timePaint;
        mBackgroundBitmap = backgroundBitmap;
        mCalendar = calendar;
    }

    public void draw(Canvas canvas, Rect bounds, boolean inAmbientMode) {
        if (inAmbientMode) {
            drawAmbientModeBackground(canvas, bounds);
        } else {
            drawRegularBackground(canvas, bounds);
        }

        String timeText = generateTimeText();
        canvas.drawText(timeText, computeCenterX(timeText, bounds), 80, mTimePaint);
    }

    private void drawAmbientModeBackground(Canvas canvas, Rect bounds) {
        Paint blackBackgroundPaint = new Paint();
        blackBackgroundPaint.setColor(Color.BLACK);
        canvas.drawRect(bounds, blackBackgroundPaint);
    }

    private void drawRegularBackground(Canvas canvas, Rect bounds) {
        int width = bounds.width();
        int height = bounds.height();

        if (mBackgroundScaledBitmap == null
                || mBackgroundScaledBitmap.getWidth() != width
                || mBackgroundScaledBitmap.getHeight() != height) {
            mBackgroundScaledBitmap =
                    Bitmap.createScaledBitmap(mBackgroundBitmap, width, height, true);
        }
        canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);
    }

    private String generateTimeText() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        return String.format(TIME_FORMAT, mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.MINUTE));
    }

    private float computeCenterX(String timeText, Rect bounds) {
        float centerX = bounds.exactCenterX();
        float timeLength = mTimePaint.measureText(timeText);
        return centerX - (timeLength / 2.0f);
    }

    public void onAmbientModeChanged(boolean inAmbientMode) {
        if (inAmbientMode) {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            mBackgroundBitmap =
                    Bitmap.createBitmap(mBackgroundBitmap.getWidth(), mBackgroundBitmap.getHeight(), conf);
        } else {

        }
    }
}
