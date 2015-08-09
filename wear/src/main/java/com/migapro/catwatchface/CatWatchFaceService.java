package com.migapro.catwatchface;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.SurfaceHolder;

import java.util.Calendar;

public class CatWatchFaceService extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private Calendar mCalendar;
        
        private Paint mTimePaint;
        private Bitmap mBackgroundDrawableBitmap;
        private Bitmap mBackgroundScaledBitmap;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            Resources resources = CatWatchFaceService.this.getResources();
            mBackgroundDrawableBitmap = ((BitmapDrawable)resources.getDrawable(R.drawable.cat4, null)).getBitmap();

            mTimePaint = new Paint();
            mTimePaint.setColor(Color.WHITE);
            mTimePaint.setTextSize(getResources().getDimension(R.dimen.paint_time_text));
            mTimePaint.setAntiAlias(true);

            mCalendar = Calendar.getInstance();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            int width = bounds.width();
            int height = bounds.height();

            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap =
                        Bitmap.createScaledBitmap(mBackgroundDrawableBitmap, width, height, true);
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

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
        }
    }
}
