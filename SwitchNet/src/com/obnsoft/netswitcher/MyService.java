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

package com.obnsoft.netswitcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class MyService extends Service {

    private static final String TAG = "netswitcher";
    private static final String BUTTON_CLICK_ACTION = "BUTTON_CLICK_ACTION";

    private static final int STATE_CURRENT = -1;
    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_WIFI    = 1;
    private static final int STATE_MOBILE  = 2;

    private static final int[] BUTTON_ID = {
        R.id.btn_unknown, R.id.btn_wifi, R.id.btn_mobile
    };

    private BroadcastReceiver   mReceiver;
    private RemoteViews         mRemoteViews;
    private ComponentName       mComponent;
    private WifiManager         mWifiMan;
    private ConnectivityManager mConnMan;
    private Method              mGetMobileMethod = null;
    private Method              mSetMobileMethod = null;
    private int                 mState = STATE_CURRENT;
    private long                mLastModified = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mState == STATE_CURRENT) {
                    initialize(context);
                    refreshUI(context);
                } else if (System.currentTimeMillis() - mLastModified >= 5000) {
                    Log.d(TAG, "Handle state change");
                    updateState(STATE_CURRENT);
                    refreshUI(context);
                } else {
                    Log.d(TAG, "Ignore state change");
                }
            }
        };
        registerReceiver(mReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(mReceiver,
                new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Context context = this.getApplicationContext();
        if (mState == STATE_CURRENT) {
            initialize(context);
        }

        if (BUTTON_CLICK_ACTION.equals(intent.getAction())) {
            toggleNetwork();
        }

        refreshUI(context);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initialize(Context context) {
        Intent buttonIntent = new Intent();
        buttonIntent.setAction(BUTTON_CLICK_ACTION);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, buttonIntent, 0);

        mRemoteViews = new RemoteViews(getPackageName(), R.layout.main);
        for (int i = 0; i < BUTTON_ID.length; i++) {
            mRemoteViews.setOnClickPendingIntent(BUTTON_ID[i], pendingIntent);
        }
        mComponent = new ComponentName(context, MyWidgetProvider.class);

        mWifiMan = (WifiManager)getSystemService(WIFI_SERVICE);
        mConnMan = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        Class<?> clazz = null;
        try {
            clazz = Class.forName(mConnMan.getClass().getName());
            Method[] available_methods = clazz.getDeclaredMethods();
            for (Method m : available_methods) {
                if (m.getName().contains("getMobileDataEnabled")) {
                    mGetMobileMethod = m;
                } else if (m.getName().contains("setMobileDataEnabled")) {
                    mSetMobileMethod = m;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        updateState(STATE_CURRENT);
        Log.d(TAG, "Initialized");
    }

    private void toggleNetwork() {
        if (mState == STATE_MOBILE) {
            mWifiMan.setWifiEnabled(true);
            setMobileDataEnabled(false);
            updateState(STATE_WIFI);
            Log.d(TAG, "Wi-Fi mode");
        } else {
            mWifiMan.setWifiEnabled(false);
            setMobileDataEnabled(true);
            updateState(STATE_MOBILE);
            Log.d(TAG, "Mobile mode");
        }
        mLastModified = System.currentTimeMillis();
    }

    private void refreshUI(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(mComponent, mRemoteViews);
    }

    private void updateState(int state) {
        if (state == STATE_CURRENT) {
            boolean wifi = mWifiMan.isWifiEnabled();
            boolean conn = getMobileDataEnabled();
            if (wifi && !conn) {
                state = STATE_WIFI;
            } else if (!wifi && conn) {
                state = STATE_MOBILE;
            } else {
                state = STATE_UNKNOWN;
            }
        }
        for (int i = 0; i < BUTTON_ID.length; i++) {
            mRemoteViews.setViewVisibility(BUTTON_ID[i],
                    (i == state) ? View.VISIBLE : View.INVISIBLE);
        }
        mState = state;
    }

    private boolean getMobileDataEnabled() {
        boolean ret = false;
        if (mGetMobileMethod != null) {
            try {
                ret = (Boolean) mGetMobileMethod.invoke(mConnMan);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private void setMobileDataEnabled(boolean enable) {
        if (mSetMobileMethod != null) {
            try {
                mSetMobileMethod.invoke(mConnMan, enable);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
