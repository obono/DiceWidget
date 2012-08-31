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

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class MyApplication extends Application {

    public static final int NOTICE_ID_ABOUT = 1;
    public static final int NOTICE_ID_STATS = 2;

    public static final int[] IMAGE_IDS = {
        R.id.image_dice1, R.id.image_dice2, R.id.image_dice3, R.id.image_dice4
    };

    public static final int[] TEXT_IDS = {
        R.id.text_number_white, R.id.text_number_black,
        R.id.text_number_red,   R.id.text_number_blue,
    };

    private static final String DB_NAME         = "database.db";
    private static final int    DB_VER          = 1;
    private static final String DB_TBL_HISTORY  = "history";
    private static final String DB_TBL_COUNT    = "count";
    private static final String DB_COL_ID       = "_id";
    private static final String DB_COL_DICE     = "dice";
    private static final String DB_COL_TIME     = "time";
    private static final int    DB_KEEP_ROWS    = 100;
    private static final String[] DB_COLS_COUNT =
        { "e1", "e2", "e3", "e4", "e5", "e6", "white", "black", "red", "blue" };

    private static final String PREFS_KEY_COLORS = "colors";
    private static final String PREFS_KEY_SOUND  = "sound";
    private static final String PREFS_KEY_STATS  = "statsIcon";

    private SharedPreferences   mPrefs;
    private SQLiteDatabase      mDB;

    private int[]   mDieColor = new int[4];
    private boolean mIsSndEnable;
    private boolean mShowStatsIcon;
    private int[]   mDieCount = new int[10];

    /*-----------------------------------------------------------------------*/

    class MySQLiteOpenHelper extends SQLiteOpenHelper {

        public MySQLiteOpenHelper(Context context, String name, int version) {
            super(context, name, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + DB_TBL_HISTORY + "(" +
                    DB_COL_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DB_COL_DICE + " INTEGER NOT NULL," +
                    DB_COL_TIME + " INTEGER NOT NULL)");
            StringBuffer buf = new StringBuffer("CREATE TABLE " + DB_TBL_COUNT + "(");
            for (int i = 0; i < DB_COLS_COUNT.length; i++) {
                buf.append(DB_COLS_COUNT[i]).append(" INTEGER NOT NULL");
                buf.append((i == DB_COLS_COUNT.length - 1) ? ')' : ',');
            }
            db.execSQL(buf.toString());
            db.insert(DB_TBL_COUNT, null, obtainDiceCountValues());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Do nothing.
        }
    }

    /*-----------------------------------------------------------------------*/

    public static void showNotice(Context context, int id, long count) {
        NotificationManager noticeMan =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notice = new Notification(
                R.drawable.icon, context.getText(R.string.app_name), System.currentTimeMillis());
        if (id == NOTICE_ID_ABOUT) {
            PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, AboutActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
            notice.setLatestEventInfo(context, context.getText(R.string.app_name),
                    context.getText(R.string.app_code) + " " + getVersion(context), pIntent);
            notice.flags |= Notification.FLAG_NO_CLEAR;
        }
        if (id == NOTICE_ID_STATS) {
            PendingIntent pIntent = PendingIntent.getActivity(context, 0,
                    new Intent(context, StatsActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
            String msg = (count > 0) ?
                    String.format(context.getText(R.string.msg_shook).toString(), count) : null;
            notice.setLatestEventInfo(context, context.getText(R.string.stats_title), msg, pIntent);
            notice.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        }
        noticeMan.notify(id, notice);
    }

    public static void hideNotice(Context context, int id) {
        NotificationManager noticeMan =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        noticeMan.cancel(id);
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

    /*-----------------------------------------------------------------------*/

    @Override
    public void onCreate() {
        super.onCreate();

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String ary[] = mPrefs.getString(PREFS_KEY_COLORS, "0,0,-1,-1").split(",");
        for (int i = 0; i < 4; i++) {
            try {
                mDieColor[i] = Integer.parseInt(ary[i]);
            } catch (NumberFormatException e) {
                mDieColor[i] = (i < 2) ? 0 : -1;
            }
        }
        mIsSndEnable = mPrefs.getBoolean(PREFS_KEY_SOUND, false);
        mShowStatsIcon = mPrefs.getBoolean(PREFS_KEY_STATS, false);

        try{
            mDB = new MySQLiteOpenHelper(this, DB_NAME, DB_VER).getWritableDatabase();
        } catch(SQLiteException e) {
            e.printStackTrace();
            return;
        }
        Cursor cursor = mDB.query(DB_TBL_COUNT, null, null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < DB_COLS_COUNT.length; i++) {
            mDieCount[i] = cursor.getInt(i);
        }
        cursor.close();
    }

    public int[] getDiceColor() {
        return mDieColor;
    }

    public boolean getSoundEnable() {
        return mIsSndEnable;
    }

    public boolean getShowStatsIcon() {
        return mShowStatsIcon;
    }

    public int[] getDiceCount() {
        return mDieCount;
    }

    public Cursor getShakeRecordCursor() {
        return mDB.query(DB_TBL_HISTORY, null, null, null, null, null, DB_COL_ID + " DESC");
    }

    public void saveConfig(int[] colorAry, boolean sound, boolean stats) {
        mDieColor = colorAry;
        mIsSndEnable = sound;
        mShowStatsIcon = stats;
        SharedPreferences.Editor editor = mPrefs.edit();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(colorAry[i]);
        }
        editor.putString(PREFS_KEY_COLORS, buf.toString());
        editor.putBoolean(PREFS_KEY_SOUND, sound);
        editor.putBoolean(PREFS_KEY_STATS, stats);
        editor.commit();
    }

    public long addShakeRecord(int[] colorAry, int[] levelAry) {
        int diceValue = 0;
        for (int i = 0; i < 4; i++) {
            int level = 0xFF;
            if (colorAry[i] >= 0) {
                mDieCount[levelAry[i] / 2]++;
                mDieCount[6 + colorAry[i]]++;
                level = levelAry[i] + colorAry[i] * 12;
            }
            diceValue |= level << (i * 8);
        }

        long id = 0;
        mDB.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DB_COL_DICE, diceValue);
            values.put(DB_COL_TIME, System.currentTimeMillis());
            id = mDB.insert(DB_TBL_HISTORY, null, values);
            if (id > DB_KEEP_ROWS) {
                mDB.delete(DB_TBL_HISTORY,
                        DB_COL_ID + " <= " + String.valueOf(id - DB_KEEP_ROWS), null);
            }
            mDB.update(DB_TBL_COUNT, obtainDiceCountValues(), null, null);
            mDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.endTransaction();
        }
        return id;
    }

    private ContentValues obtainDiceCountValues() {
        ContentValues values = new ContentValues();
        for (int i = 0; i < DB_COLS_COUNT.length; i++) {
            values.put(DB_COLS_COUNT[i], mDieCount[i]);
        }
        return values;
    }

}
