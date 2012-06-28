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
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class MyApplication extends Application {

    private SheetData mData;

    public MyApplication() {
        super();
        mData = new SheetData();
    }

    protected SheetData getSheetData() {
        return mData;
    }

    /*----------------------------------------------------------------------*/

    static public CharSequence getDateString(Context context, Calendar cal) {
        return DateFormat.getDateFormat(context).format(cal.getTime());
    }

    static public void showDatePickerDialog(
            Context context, Calendar cal, DatePickerDialog.OnDateSetListener listener) {
        new DatePickerDialog(context, listener, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    static public void showYesNoDialog(
            Context context, int iconId, int titleId, int msgId,
            DialogInterface.OnClickListener listener) {
        showYesNoDialog(context, iconId, context.getText(titleId), msgId, listener);
    }

    static public void showYesNoDialog(
            Context context, int iconId, CharSequence title, int msgId,
            DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(title)
                .setMessage(msgId)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    static public void showShareDialog(
            Context context, int iconId, CharSequence title, CharSequence msg) {
        new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        
                    }
                })
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    static public void showSingleChoiceDialog(
            Context context, int iconId, int titleId, String[] items,
            int choice, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(titleId)
                .setSingleChoiceItems(items, choice, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    static public void showMultiChoiceDialog(
            Context context, int iconId, int titleId, String[] items,
            final boolean[] choices, DialogInterface.OnClickListener listener) {
        DialogInterface.OnMultiChoiceClickListener
        listener2 = new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                choices[which] = isChecked;
            }
        };
        new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(titleId)
                .setMultiChoiceItems(items, choices, listener2)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    static public void showCustomDialog(
            Context context, int iconId, int titleId, View view,
            DialogInterface.OnClickListener listener) {
        final AlertDialog dlg = new AlertDialog.Builder(context)
                .setIcon(iconId)
                .setTitle(titleId)
                .setView(view)
                .setPositiveButton(android.R.string.ok, listener)
                .create();
        if (listener != null) {
            dlg.setButton(AlertDialog.BUTTON_NEGATIVE, context.getText(android.R.string.cancel),
                    (DialogInterface.OnClickListener) null);
        }
        if (view instanceof EditText) {
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        dlg.getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });
        }
        dlg.show();
    }

    /*----------------------------------------------------------------------*/

}
