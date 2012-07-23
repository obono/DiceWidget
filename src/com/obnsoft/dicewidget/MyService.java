/*
 * Copyright (C) 2012 OBN-soft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.obnsoft.dicewidget;

import java.util.Random;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class MyService extends Service {

    public static final String ACTION_SHAKE  = "com.obnsoft.dicewidget.action.SHAKE";

    private static final String TAG = "DiceWidget";
    private static final int LOOP_COUNT = 10;
    private static final int TIMER_INTERVAL = 50;

    private static final int[] IMAGE_IDS = {
        R.id.image_dice1, R.id.image_dice2, R.id.image_dice3, R.id.image_dice4
    };
    private static final int[] TEXT_IDS = {
        R.id.text_number_white, R.id.text_number_black,
        R.id.text_number_red,   R.id.text_number_blue,
    };

    private static boolean  sIsShaking = false;
    private static Object   sIsShakingLock = new Object();

    private int[]   mDieLevel = new int[4];
    private int[]   mDieColor = new int[4];
    private boolean mIsSndEnable = false;

    private RemoteViews     mRemoteViews;
    private ComponentName   mComponent;
    private Random          mRandom;

    @Override
    public IBinder onBind(Intent intent) {
        myLog("onBind " + intent.getAction());
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        myLog("onStart " + intent.getAction());

        synchronized (sIsShakingLock) {
            if (sIsShaking) {
                stopSelf();
                return;
            }
        }

        Context context = getBaseContext();
        initialize(context);
        if (ACTION_SHAKE.equals(intent.getAction())) {
            shakeDice(context);
        } else {
            updateDice(context, true);
            showNumbers(context);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myLog("onDestroy");
    }

    private void initialize(Context context) {
        myLog("initialize");
        mIsSndEnable = ConfigActivity.loadConfig(context, mDieColor);

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.main);
        mComponent = new ComponentName(context, MyWidgetProvider.class);
        mRandom = new Random();

        PendingIntent pendingIntent;
        pendingIntent = PendingIntent.getService(context, 0, new Intent(ACTION_SHAKE), 0);
        mRemoteViews.setOnClickPendingIntent(R.id.group_dice, pendingIntent);
        pendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(this, ConfigActivity.class), 0);
        mRemoteViews.setOnClickPendingIntent(R.id.button_config, pendingIntent);
        controlVisibilty(context);
    }

    private void controlVisibilty(Context context) {
        int[] count = new int[4];
        for (int i = 0; i < 4; i++) {
            if (mDieColor[i] >= 0) {
                count[mDieColor[i]]++;
                mRemoteViews.setViewVisibility(IMAGE_IDS[i], View.VISIBLE);
            } else {
                mRemoteViews.setViewVisibility(IMAGE_IDS[i], View.GONE);
            }
        }
        for (int i = 0; i < 4; i++) {
            mRemoteViews.setViewVisibility(TEXT_IDS[i],
                    (count[i] >= 2) ? View.VISIBLE : View.GONE);
        }
    }

    private void shakeDice(final Context context) {
        synchronized (sIsShakingLock) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    myLog("shakeDice start");
                    sIsShaking = true;
                    hideNumbers(context);
                    MediaPlayer player = null;
                    if (mIsSndEnable) {
                        player = MediaPlayer.create(context, R.raw.sound);
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.start();
                    }
                    for (int i = LOOP_COUNT; i > 0; i--) {
                        updateDice(context, ((i & 1) == 1));
                        try {
                            Thread.sleep(TIMER_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    showNumbers(context);
                    if (mIsSndEnable) {
                        player.release();
                    }
                    sIsShaking = false;
                    myLog("shakeDice end");
                }
            }).start();
        }
    }

    private void updateDice(Context context, boolean upeer) {
        for (int i = 0; i < 4; i++) {
            if (mDieColor[i] >= 0) {
                mDieLevel[i] = mRandom.nextInt(12);
                mRemoteViews.setInt(IMAGE_IDS[i], "setImageLevel",
                        mDieLevel[i] + mDieColor[i] * 12);
            }
        }
        mRemoteViews.setViewVisibility(R.id.space_top, upeer ? View.GONE : View.VISIBLE);
        mRemoteViews.setViewVisibility(R.id.space_bottom, upeer ? View.VISIBLE : View.GONE);
        refreshUI(context);
    }

    private void hideNumbers(Context context) {
        for (int i = 0; i < 4; i++) {
            mRemoteViews.setTextViewText(TEXT_IDS[i], null);
        }
    }

    private void showNumbers(Context context) {
        int[] value = new int[4];
        for (int i = 0; i < 4; i++) {
            if (mDieColor[i] >= 0) {
                value[mDieColor[i]] += mDieLevel[i] / 2 + 1;
            }
        }
        for (int i = 0; i < 4; i++) {
            mRemoteViews.setTextViewText(TEXT_IDS[i], String.valueOf(value[i]));
        }
        refreshUI(context);
    }

    private void refreshUI(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(mComponent, mRemoteViews);
    }

    private void myLog(String msg) {
        Log.d(TAG, msg);
    }

}
