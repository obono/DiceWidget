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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfigActivity extends Activity {

    private static final int[] GROUP_IDS = {
        R.id.group_config_dice1, R.id.group_config_dice2,
        R.id.group_config_dice3, R.id.group_config_dice4,
    };
    private static final int[] STRING_IDS = {
        R.string.dice_white, R.string.dice_black,
        R.string.dice_red,   R.string.dice_blue,
    };
    private static final int[] DIE_LEVELS = { 2, 18, 28, 44 };

    private MyApplication   mApp;
    private Button          mButtonOK;
    private CheckBox        mCheckBoxSound;
    private CheckBox        mCheckBoxStats;
    private int[]           mDieColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config);

        mApp = (MyApplication) getApplication();
        mButtonOK = (Button) findViewById(R.id.button_config_ok);
        mCheckBoxSound = (CheckBox) findViewById(R.id.checkbox_config_sound);
        mCheckBoxStats = (CheckBox) findViewById(R.id.checkbox_config_stats);

        mDieColor = mApp.getDiceColor().clone();
        for (int i = 0; i < 4; i++) {
            setDiceInfo(findViewById(GROUP_IDS[i]), mDieColor[i]);
        }
        mCheckBoxSound.setChecked(mApp.getSoundEnable());
        mCheckBoxStats.setChecked(mApp.getShowStatsIcon());
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
            mApp.saveConfig(mDieColor, mCheckBoxSound.isChecked(), mCheckBoxStats.isChecked());
            startService(new Intent(this, MyService.class));
        }
        finish();
    }

    private void setDiceInfo(View v, int color) {
        ImageView iv = (ImageView) v.findViewById(R.id.image_config_dice);
        TextView tv = (TextView) v.findViewById(R.id.text_config_dice);
        if (color >= 0) {
            iv.setVisibility(View.VISIBLE);
            iv.getDrawable().setLevel(DIE_LEVELS[color]);
            tv.setText(STRING_IDS[color]);
        } else {
            iv.setVisibility(View.GONE);
            tv.setText(R.string.dice_none);
        }
    }
}
