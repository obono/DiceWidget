package com.obnsoft.view;

import android.content.Context;
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
import android.widget.Checkable;
import android.widget.GridView;
import android.widget.RelativeLayout;

public class GridColorPickerView extends GridView
        implements android.widget.AdapterView.OnItemClickListener {

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    class ColorSampleView extends View implements Checkable {

        boolean mChecked = false;
        int mSize = 0;

        public ColorSampleView(Context context) {
            super(context);
        }

        public void setSize(int size) {
            mSize = size;
        }

        @Override
        public void onDraw(Canvas c) {
            super.onDraw(c);
            if (mChecked) {
                
            }
        }

        @Override
        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(mSize, mSize);
        }

        @Override
        public boolean isChecked() {
            return mChecked;
        }

        @Override
        public void setChecked(boolean checked) {
            mChecked = checked;
        }

        @Override
        public void toggle() {
            mChecked = !mChecked;
        }
    }

    class MyAdapter extends ArrayAdapter<Integer> {

        class ViewHolder {
            ColorSampleView cv;
        }

        private Context mContext;

        public MyAdapter(Context context, Integer[] objects) {
            super(context, 0, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                RelativeLayout rl = new RelativeLayout(mContext);
                rl.setGravity(Gravity.CENTER);
                int pad = mBoxSize / 10;
                rl.setPadding(pad, pad, pad, pad);
                ColorSampleView cv = new ColorSampleView(mContext);
                rl.addView(cv);
                convertView = rl;
                holder = new ViewHolder();
                holder.cv = cv;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.cv.setSize(mBoxSize);
            holder.cv.setBackgroundColor(getItem(position));
            return convertView;
        }
    }

    private int mCurColor;
    private int mBoxSize;
    private OnColorChangedListener mListener;
    static Integer[] sPresets = new Integer[65];

    static {
        float[] hsv = new float[3];
        for (int i = 0; i < 12; i++) {
            hsv[0] = i * 30;
            for (int j = 0; j < 5; j++) {
                hsv[1] = (j > 2) ? (6 - j) / 4f : 1f;
                hsv[2] = (j < 2) ? (2 + j) / 4f : 1f;
                sPresets[i * 5 + j] = Color.HSVToColor(hsv);
            }
        }
        hsv[1] = 0f;
        for (int i = 0; i < 5; i++) {
            hsv[2] = i / 4f;
            sPresets[60 + i] = Color.HSVToColor(hsv);
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
        mBoxSize = Math.min(disp.getWidth(), disp.getHeight()) / 8;

        //setVerticalScrollBarEnabled(true);
        setVerticalFadingEdgeEnabled(true);
        setNumColumns(5);
        MyAdapter adapter = new MyAdapter(context, sPresets);
        setAdapter(adapter);
        setOnItemClickListener(this);
        setColor(Color.BLACK);
    }

    public void setListener(OnColorChangedListener l) {
        mListener = l;
    }

    public void setColor(int color) {
        mCurColor = color | 0xFF000000;
        MyAdapter adapter = (MyAdapter) getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (mCurColor == adapter.getItem(i)) {
                onItemClick(this, null, i, 0);
                return;
            }
        }
    }

    public int getColor() {
        return mCurColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            widthMeasureSpec =
                    MeasureSpec.makeMeasureSpec(mBoxSize * 6, MeasureSpec.EXACTLY);
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(mBoxSize * 3, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MyAdapter adapter = (MyAdapter) parent.getAdapter();
        mCurColor = adapter.getItem(position);
        if (mListener != null) {
            mListener.colorChanged(mCurColor);
        }
    }

}
