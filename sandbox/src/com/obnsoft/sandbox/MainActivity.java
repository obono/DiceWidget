package com.obnsoft.sandbox;

import com.obnsoft.view.GridColorPickerView;
import com.obnsoft.view.HSVColorPickerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void onPickHSVColor(View v) {
        LinearLayout ll = new LinearLayout(this);
        final TextView tv = new TextView(this);
        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lp.weight = 1.0f;
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        ll.addView(tv);

        final HSVColorPickerView cpv = new HSVColorPickerView(this);
        cpv.setListener(new HSVColorPickerView.OnColorChangedListener() {
            @Override
            public void colorChanged(int color, float hue, float sat, float val) {
                tv.setText(String.format("#%06X", color & 0xFFFFFF));
                tv.setBackgroundColor(color);
                tv.setTextColor((HSVColorPickerView.calcBrightness(color) < 0.5) ?
                        Color.WHITE : Color.BLACK);
            }
        });
        cpv.setColor(getTitleColor());
        ll.addView(cpv);

        new AlertDialog.Builder(this)
                .setView(ll)
                .setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int witch) {
                        setTitleColor(cpv.getColor());
                    }
                }).show();
     }

    public void onPickGridColor(View v) {
        LinearLayout ll = new LinearLayout(this);
        final TextView tv = new TextView(this);
        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lp.weight = 1.0f;
        tv.setLayoutParams(lp);
        tv.setGravity(Gravity.CENTER);
        ll.addView(tv);

        final GridColorPickerView cpv = new GridColorPickerView(this);
        cpv.setListener(new GridColorPickerView.OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                tv.setText(String.format("#%06X", color & 0xFFFFFF));
                tv.setBackgroundColor(color);
                tv.setTextColor((HSVColorPickerView.calcBrightness(color) < 0.5) ?
                        Color.WHITE : Color.BLACK);
            }
        });
        cpv.setColor(getTitleColor());
        ll.addView(cpv);

        new AlertDialog.Builder(this)
                .setView(ll)
                .setPositiveButton("OK", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int witch) {
                        setTitleColor(cpv.getColor());
                    }
                }).show();
     }
}
