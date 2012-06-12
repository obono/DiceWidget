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

package com.obnsoft.sandbox;

import com.obnsoft.view.ColorPickerInterface;
import com.obnsoft.view.GridColorPickerView;
import com.obnsoft.view.HSVColorPickerView;
import com.obnsoft.view.RGBColorPickerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int PICK_FILE_REQUEST = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == PICK_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra(MyFilePickerActivity.INTENT_EXTRA_PATH);
                TextView tv = (TextView) findViewById(R.id.text_hello);
                tv.setText(path);
            }
        }
    }

    public void onPickRGBColor(View v) {
        pickColor(new RGBColorPickerView(this));
    }

    public void onPickHSVColor(View v) {
        pickColor(new HSVColorPickerView(this));
    }

    public void onPickGridColor(View v) {
        pickColor(new GridColorPickerView(this));
    }

    public void onPickFile(View v) {
        String path = Environment.getExternalStorageDirectory().getPath();
        Intent intent = new Intent(this, MyFilePickerActivity.class);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_PATH, path);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_EXTENSION, ".xml");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void pickColor(final View cpv) {
        final ColorPickerInterface cpi;
        try {
            cpi = (ColorPickerInterface) cpv;
        } catch (ClassCastException e) {
            throw new ClassCastException(cpv.toString() +
                    " must implement ColorPickerInterface");
        }
        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f);
        LinearLayout ll = new LinearLayout(this);
        //ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout ll2 = new LinearLayout(this);
        ll2.setOrientation(LinearLayout.VERTICAL);
        ll2.setLayoutParams(lp);
        final TextView tv1 = new TextView(this);
        tv1.setLayoutParams(lp);
        tv1.setGravity(Gravity.CENTER);
        ll2.addView(tv1);
        final TextView tv2 = new TextView(this);
        tv2.setLayoutParams(lp);
        tv2.setGravity(Gravity.CENTER);
        ll2.addView(tv2);
        ll.addView(ll2);
        cpi.setListener(new ColorPickerInterface.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                setTextViewColor(tv2, color);
            }
        });
        ll.addView(cpv);

        final int color = getTitleColor() | 0xFF000000;
        setTextViewColor(tv1, color);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cpi.setColor(color);
            }
        });
        cpi.setColor(color);

        new AlertDialog.Builder(this)
        .setView(ll)
        .setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int witch) {
                setTitleColor(cpi.getColor());
            }
        }).show();
    }

    private void setTextViewColor(TextView tv, int color) {
        tv.setText(String.format("#%06X", color & 0xFFFFFF));
        tv.setBackgroundColor(color);
        tv.setTextColor((HSVColorPickerView.calcBrightness(color) < 0.5) ?
                Color.WHITE : Color.BLACK);
    }
}
