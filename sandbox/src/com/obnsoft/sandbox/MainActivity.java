package com.obnsoft.sandbox;

import com.obnsoft.view.ColorPickerInterface;
import com.obnsoft.view.GridColorPickerView;
import com.obnsoft.view.HSVColorPickerView;
import com.obnsoft.view.RGBColorPickerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
