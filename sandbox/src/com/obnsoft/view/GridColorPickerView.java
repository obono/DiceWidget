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

package com.obnsoft.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;

public class GridColorPickerView extends GridView
        implements ColorPickerInterface, AdapterView.OnItemClickListener {

    class MyAdapter extends ArrayAdapter<Integer> {

        class ColorSampleView extends View {

            int mSize = 0;
            boolean mChecked = false;

            public ColorSampleView(Context context) {
                super(context);
            }

            public void setChecked(boolean checked) {
                mChecked = checked;
            }

            public void setSize(int size) {
                mSize = size;
            }

            @Override
            public void onDraw(Canvas c) {
                super.onDraw(c);
                if (mChecked) {
                    c.drawBitmap(mBmpSelected,
                            (mSize - mBmpSelected.getWidth()) / 2,
                            (mSize - mBmpSelected.getHeight()) / 2, null);
                }
            }

            @Override
            public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                setMeasuredDimension(mSize, mSize);
            }
        }

        private int mSelected = -1;
        private Context mContext;
        private Bitmap mBmpSelected;

        public MyAdapter(Context context, Integer[] objects) {
            super(context, 0, objects);
            mContext = context;
            mBmpSelected = BitmapFactory.decodeResource(
                    getResources(), android.R.drawable.radiobutton_on_background);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ColorSampleView cv;
            if (convertView == null) {
                RelativeLayout rl = new RelativeLayout(mContext);
                rl.setGravity(Gravity.CENTER);
                int pad = mBoxSize / 10;
                rl.setPadding(pad, pad, pad, pad);
                cv = new ColorSampleView(mContext);
                rl.addView(cv);
                convertView = rl;
                convertView.setTag(cv);
            } else {
                cv = (ColorSampleView) convertView.getTag();
            }
            cv.setSize(mBoxSize);
            cv.setChecked(position == mSelected);
            cv.setBackgroundColor(getItem(position));
            return convertView;
        }

        public void setSelected(int position) {
            mSelected = position;
            notifyDataSetChanged();
        }

        public int getSelection() {
            return mSelected;
        }
    }

    private int mCurColor;
    private int mMinimumSize;
    private boolean mMeasured = false;
    private int mBoxSize;
    private OnColorChangedListener mListener;
    static Integer[] sPresets = new Integer[65];

    static {
        float[] hsv = new float[3];
        hsv[0] = hsv[1] = 0f;
        for (int i = 0; i < 5; i++) {
            hsv[2] = i / 4f;
            sPresets[i] = Color.HSVToColor(hsv);
        }
        for (int i = 0; i < 12; i++) {
            hsv[0] = i * 30;
            for (int j = 0; j < 5; j++) {
                hsv[1] = (j > 2) ? (6 - j) / 4f : 1f;
                hsv[2] = (j < 2) ? (2 + j) / 4f : 1f;
                sPresets[i * 5 + j + 5] = Color.HSVToColor(hsv);
            }
        }
    }

    public GridColorPickerView(Context context) {
        this(context, null);
    }

    public GridColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        mMinimumSize = Math.min(disp.getWidth(), disp.getHeight()) / 2;

        setVerticalScrollBarEnabled(true);
        setVerticalFadingEdgeEnabled(true);
        setNumColumns(5);
        MyAdapter adapter = new MyAdapter(context, sPresets);
        setAdapter(adapter);
        setOnItemClickListener(this);

        setColor(Color.BLACK);
    }

    @Override
    public void setColor(int color) {
        MyAdapter adapter = (MyAdapter) getAdapter();
        int target = 0;
        double minDiff = 256 * 256 * 2;
        for (int i = 0; i < adapter.getCount(); i++) {
            int targetColor = adapter.getItem(i);
            double diff = colorDiff(color, targetColor);
            if (diff < minDiff) {
                mCurColor = targetColor;
                minDiff = diff;
                target = i;
            }
        }
        adapter.setSelected(target);
        if (mMeasured) {
            smoothScrollToPosition(target);
        } else {
            setSelection(target);
        }
        if (mListener != null) {
            mListener.colorChanged(mCurColor);
        }
    }

    private double colorDiff(int col1, int col2) {
        int rd = Color.red(col1) - Color.red(col2);
        int gd = Color.green(col1) - Color.green(col2);
        int bd = Color.blue(col1) - Color.blue(col2);
        return Math.sqrt(rd * rd + gd * gd + bd * bd);
    }

    @Override
    public int getColor() {
        return mCurColor;
    }

    @Override
    public void setListener(OnColorChangedListener l) {
        mListener = l;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(widthMeasureSpec) : mMinimumSize;
        int height = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(heightMeasureSpec) : mMinimumSize;
        mBoxSize = width / 6;
        mMeasured = true;
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MyAdapter adapter = (MyAdapter) parent.getAdapter();
        mCurColor = adapter.getItem(position);
        adapter.setSelected(position);
        if (mListener != null) {
            mListener.colorChanged(mCurColor);
        }
    }

}
