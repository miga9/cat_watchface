package com.migapro.catwatchface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.migapro.catwatchface.util.Constants;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class CatWatchFaceService extends CanvasWatchFaceService {

    private static final String TAG = "Cat Wear";
    private static final long TICK_PERIOD = TimeUnit.MINUTES.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private static final int MSG_UPDATE_TIME = 0;

        private GoogleApiClient mGoogleApiClient;
        private CatWatchFace mWatchFace;
        private boolean mLowBitAmbient;

        private final Handler mTimeTickHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        Log.d(TAG, "time update");

                        invalidate();
                        if (shouldTimerBeRunning()) {
                            sendDelayedMessage(MSG_UPDATE_TIME, TICK_PERIOD);
                        }
                        break;
                }
            }
        };

        public DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvents) {
                Log.d(TAG, "Data changed: " + dataEvents);
                for (DataEvent event : dataEvents) {
                    Log.d(TAG, "Data received: " + event.getDataItem().getUri());

                    if (event.getType() == DataEvent.TYPE_CHANGED &&
                            event.getDataItem().getUri().getPath().equals(Constants.KEY_DATAMAP_PATH)) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                        Asset profileAsset = dataMapItem.getDataMap().getAsset(Constants.KEY_ASSET);
                        Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                        mWatchFace.setBitmap(bitmap);
                    }
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            mGoogleApiClient = new GoogleApiClient.Builder(CatWatchFaceService.this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();

            mWatchFace = new CatWatchFace(CatWatchFaceService.this);
        }

        private void sendDelayedMessage(int msg, long interval) {
            long timeMs = System.currentTimeMillis();
            long delayMs = interval - (timeMs % interval);
            mTimeTickHandler.sendEmptyMessageDelayed(msg, delayMs);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mWatchFace.onAmbientModeChanged(mLowBitAmbient, inAmbientMode);
            invalidate();
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            mWatchFace.draw(canvas, bounds, isInAmbientMode());
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            updateTimer();
        }

        private void updateTimer() {
            mTimeTickHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mTimeTickHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mTimeTickHandler.removeMessages(MSG_UPDATE_TIME);
            mGoogleApiClient.disconnect();
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, onDataChangedListener);
            Log.d(TAG, "onConnected");
        }

        @Override
        public void onConnectionSuspended(int i) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
        }

        private Bitmap loadBitmapFromAsset(Asset asset) {
            if (asset == null) {
                throw new IllegalArgumentException("Asset must be non-null");
            }

            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await().getInputStream();

            if (assetInputStream == null) {
                Log.w(TAG, "Requested an unknown Asset.");
                return null;
            }

            return BitmapFactory.decodeStream(assetInputStream);
        }
    }
}
