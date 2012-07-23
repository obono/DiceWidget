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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyWidgetProvider extends AppWidgetProvider {

    protected static boolean sLaunched = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager awm, int[] awi) {
        super.onUpdate(context, awm, awi);
        if (!sLaunched) {
            context.startService(new Intent(context, MyService.class));
            sLaunched = true;
        }
        Log.i("HOGE", "onUpdate");
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        sLaunched = false;
        context.stopService(new Intent(context, MyService.class));
        Log.i("HOGE", "onDeleted");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.i("HOGE", "onDisabled");
    }
}
