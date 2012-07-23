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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfigActivity extends Activity {

    private static final String PREFS_KEY_COLORS = "colors";
    private static final String PREFS_KEY_SOUND  = "sound";
    private static final int[] GROUP_IDS = {
        R.id.group_config_dice1, R.id.group_config_dice2,
        R.id.group_config_dice3, R.id.group_config_dice4,
    };
    private static final int[] STRING_IDS = {
        R.string.dice_white, R.string.dice_black,
        R.string.dice_red,   R.string.dice_blue,
    };

    private int[]   mDieColor = new int[4];
    private Button      mButtonOK;
    private CheckBox    mCheckBoxSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);

        boolean sound = loadConfig(this, mDieColor);
        mButtonOK = (Button) findViewById(R.id.button_config_ok);
        mCheckBoxSound = (CheckBox) findViewById(R.id.checkbox_config_sound);
        for (int i = 0; i < 4; i++) {
            setDiceInfo(findViewById(GROUP_IDS[i]), mDieColor[i]);
        }
        mCheckBoxSound.setChecked(sound);
    }

    public static boolean loadConfig(Context context, int[] colors) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ary[] = prefs.getString(PREFS_KEY_COLORS, "0,0,-1,-1").split(",");
        for (int i = 0; i < 4; i++) {
            try {
                colors[i] = Integer.parseInt(ary[i]);
            } catch (NumberFormatException e) {
                colors[i] = (i < 2) ? 0 : -1;
            }
        }
        return prefs.getBoolean(PREFS_KEY_SOUND, false);
    }

    public static void saveConfig(Context context, int[] colors, boolean sound) {
        SharedPreferences.Editor editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(colors[i]);
        }
        editor.putString(PREFS_KEY_COLORS, buf.toString());
        editor.putBoolean(PREFS_KEY_SOUND, sound);
        editor.commit();
    }

    public void onClickDice(View v) {
        int count = 0;
        for (int i = 0; i < 4; i++) {
            if (v.getId() == GROUP_IDS[i]) {
                if (++mDieColor[i] >= 4) {
                    mDieColor[i] = -1;
                }
                setDiceInfo(v, mDieColor[i]);
            }
            if (mDieColor[i] >= 0) {
                count++;
            }
        }
        mButtonOK.setEnabled((count > 0));
    }

    public void onClickButton(View v) {
        if (v == mButtonOK) {
            saveConfig(this, mDieColor, mCheckBoxSound.isChecked());
            Intent intent = new Intent(this, MyService.class);
            startService(intent);
        }
        finish();
    }

    private void setDiceInfo(View v, int color) {
        ImageView iv = (ImageView) v.findViewById(R.id.image_config_dice);
        TextView tv = (TextView) v.findViewById(R.id.text_config_dice);
        if (color >= 0) {
            iv.setVisibility(View.VISIBLE);
            iv.getDrawable().setLevel(color * 12);
            tv.setText(STRING_IDS[color]);
        } else {
            iv.setVisibility(View.GONE);
            tv.setText(R.string.dice_none);
        }
    }
}
