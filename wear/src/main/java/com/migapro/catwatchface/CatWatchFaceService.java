package com.migapro.catwatchface;

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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class CatWatchFaceService extends CanvasWatchFaceService {

    private static final long TICK_PERIOD = TimeUnit.MINUTES.toMillis(1);
    private static final long RETRIEVE_NEW_IMAGES_PERIOD = TimeUnit.MINUTES.toMillis(2); // TODO Short interval for testing

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        private static final int MSG_UPDATE_TIME = 0;
        private static final int MSG_RETRIEVE_NEW_IMAGES = 1;

        private GoogleApiClient mGoogleApiClient;
        private CatWatchFace mWatchFace;
        private boolean mLowBitAmbient;

        private final Handler mTimeTickHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            sendDelayedMessage(MSG_UPDATE_TIME, TICK_PERIOD);
                        }
                        break;
                    case MSG_RETRIEVE_NEW_IMAGES:
                        sendNewImagesRequest();
                        sendDelayedMessage(MSG_RETRIEVE_NEW_IMAGES, RETRIEVE_NEW_IMAGES_PERIOD);
                        break;
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

            mWatchFace = new CatWatchFace(CatWatchFaceService.this);
            sendDelayedMessage(MSG_RETRIEVE_NEW_IMAGES, RETRIEVE_NEW_IMAGES_PERIOD);
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

        private void sendNewImagesRequest() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, nodes.getNodes().get(0).getId(), "/retrieve_new_images", new byte[0])
                            .setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                                @Override
                                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                                    Log.d("Cat WatchFace", "Status: " + sendMessageResult.getStatus());
                                }
                            });
                }
            });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mTimeTickHandler.removeMessages(MSG_UPDATE_TIME);
        }

        @Override
        public void onConnected(Bundle bundle) {
        }

        @Override
        public void onConnectionSuspended(int i) {
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
        }
    }
}
