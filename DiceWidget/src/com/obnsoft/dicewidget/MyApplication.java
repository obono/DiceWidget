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
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

public class MyApplication extends Application {

    public static final int YELLOW_MODE_CONFIG = 0;
    public static final int YELLOW_MODE_STATS  = 1;

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
    private static final String[] DB_COLS_COLOR = { "white", "black", "red", "blue" };

    private static final String PREFS_KEY_COLORS = "colors";
    private static final String PREFS_KEY_SOUND  = "sound";
    private static final String PREFS_KEY_YELLOW = "yellow";

    private SharedPreferences   mPrefs;
    private SQLiteDatabase      mDB;

    private int[]   mDieColor = new int[4];
    private boolean mIsSndEnable;
    private int     mYellowMode;
    private int[]   mDieCount = new int[4];

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
            db.execSQL("CREATE TABLE " + DB_TBL_HISTORY + "(" +
                    DB_COLS_COLOR[0] + " INTEGER NOT NULL," +
                    DB_COLS_COLOR[1] + " INTEGER NOT NULL," +
                    DB_COLS_COLOR[2] + " INTEGER NOT NULL," +
                    DB_COLS_COLOR[3] + " INTEGER NOT NULL)");
            db.insert(DB_TBL_COUNT, null, getDiceCountValues());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Do nothing.
        }
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
        mYellowMode = mPrefs.getInt(PREFS_KEY_YELLOW, YELLOW_MODE_CONFIG);

        try{
            mDB = new MySQLiteOpenHelper(this, DB_NAME, DB_VER).getWritableDatabase();
        } catch(SQLiteException e) {
            e.printStackTrace();
            return;
        }
        Cursor cursor = mDB.query(DB_TBL_COUNT, null, null, null, null, null, null);
        cursor.moveToFirst();
        for (int i = 0; i < 4; i++) {
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

    public int getYellowMode() {
        return mYellowMode;
    }

    public void saveConfig(int[] colorAry, boolean sound) {
        SharedPreferences.Editor editor = mPrefs.edit();
        StringBuffer buf = new StringBuffer();
        mDieColor = colorAry;
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(mDieColor[i]);
        }
        editor.putString(PREFS_KEY_COLORS, buf.toString());
        editor.putBoolean(PREFS_KEY_SOUND, sound);
        editor.commit();
    }

    public void setYellowMode(int mode) {
        mYellowMode = mode;
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(PREFS_KEY_YELLOW, mode);
        editor.commit();
    }

    public void addShakeRecord(int[] colorAry, int[] levelAry) {
        int diceValue = 0;
        for (int i = 0; i < 4; i++) {
            int value = 0xFF;
            if (colorAry[i] >= 0) {
                mDieCount[colorAry[i]]++;
                value = levelAry[i];
            }
            diceValue |= value << (i * 8);
        }

        mDB.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DB_COL_DICE, diceValue);
            values.put(DB_COL_TIME, System.currentTimeMillis());
            long id = mDB.insert(DB_TBL_HISTORY, null, values);
            if (id > 100) {
                mDB.delete(DB_TBL_HISTORY, "_id <= ?", new String[]{String.valueOf(id - 100)});
            }
            mDB.update(DB_TBL_COUNT, getDiceCountValues(), null, null);
            mDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDB.endTransaction();
        }
    }

    public Cursor getStatsInfo(int[] countAry) {
        return mDB.query(DB_TBL_HISTORY, null, null, null, null, null, null);
    }

    public int[] getStatsCount() {
        return mDieCount;
    }

    private ContentValues getDiceCountValues() {
        ContentValues values = new ContentValues();
        for (int i = 0; i < 4; i++) {
            values.put(DB_COLS_COLOR[i], mDieCount[i]);
        }
        return values;
    }
}
