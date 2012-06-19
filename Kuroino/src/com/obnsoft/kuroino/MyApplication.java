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

package com.obnsoft.kuroino;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class MyApplication extends Application {

    public MyApplication() {
        super();
    }

    /*----------------------------------------------------------------------*/

    static public void showDatePickerDialog(
            Context context, Calendar cal, DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(context, listener, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    static public void showYesNoDialog(
            Context context, int iconId, int titleId, int msgId,
            DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(titleId)
                .setMessage(msgId)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    static public void showCustomDialog(
            Context context, int iconId, int titleId, View view,
            DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(titleId)
                .setView(view)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /*----------------------------------------------------------------------*/

}
