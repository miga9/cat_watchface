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

    public void draw(Canvas canvas, Rect bounds) {
        int width = bounds.width();
        int height = bounds.height();

        if (mBackgroundScaledBitmap == null
                || mBackgroundScaledBitmap.getWidth() != width
                || mBackgroundScaledBitmap.getHeight() != height) {
            mBackgroundScaledBitmap =
                    Bitmap.createScaledBitmap(mBackgroundBitmap, width, height, true);
        }
        canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);

        String timeText = generateTimeText();
        canvas.drawText(timeText, computeCenterX(timeText, bounds), 80, mTimePaint);
    }

    private String generateTimeText() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        return mCalendar.get(Calendar.HOUR) + ":" + mCalendar.get(Calendar.MINUTE);
    }

    private float computeCenterX(String timeText, Rect bounds) {
        float centerX = bounds.exactCenterX();
        float timeLength = mTimePaint.measureText(timeText);
        return centerX - (timeLength / 2.0f);
    }
}
