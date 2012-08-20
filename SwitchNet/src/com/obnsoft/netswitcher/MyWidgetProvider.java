/*
 * Copyright (C) 2011-2012 OBN-soft
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class MyWidgetProvider extends AppWidgetProvider {

    public static final int NOTICE_ID_ABOUT = 1;

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        NotificationManager noticeMan =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notice = new Notification(
                R.drawable.icon, context.getText(R.string.app_name), System.currentTimeMillis());
        PendingIntent pIntent = PendingIntent.getService(
                context, 0, new Intent(MyService.HIDE_NOTICE_ACTION), 0);
        notice.setLatestEventInfo(context, context.getText(R.string.app_name),
                context.getText(R.string.app_code) + " " + getVersion(context), pIntent);
        noticeMan.notify(NOTICE_ID_ABOUT, notice);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent intent = new Intent(context, MyService.class);
        context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        hideNotice(context);
    }

    public static void hideNotice(Context context) {
        NotificationManager noticeMan =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        noticeMan.cancel(NOTICE_ID_ABOUT);
    }

    public static String getVersion(Context context) {
        String version = null;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            version = "Version " + packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

}
