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
import android.content.SharedPreferences;
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

    private static final String PREFS_KEY_COLORS = "colors";
    private static final String PREFS_KEY_SOUND  = "sound";
    private static final String PREFS_KEY_YELLOW = "yellow";

    private SharedPreferences   mPrefs;

    private int[]   mDieColor = new int[4];
    private boolean mIsSndEnable;
    private int     mYellowMode;

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

}
