package com.obnsoft.netswitcher;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public class MyWidgetProvider extends AppWidgetProvider {

    /*@Override
    public void onEnabled(Context context) {
        Log.v(TAG, "onEnabled");
        super.onEnabled(context);
    }*/

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Intent intent = new Intent(context, MyService.class);
        context.startService(intent);
    }

    /*@Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.v(TAG, "onDeleted");
        super.onDeleted(context, appWidgetIds);
    }*/

    /*@Override
    public void onDisabled(Context context) {
        Log.v(TAG, "onDisabled");
        super.onDisabled(context);
    }*/

    /*@Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        super.onReceive(context, intent);
    }*/

}
