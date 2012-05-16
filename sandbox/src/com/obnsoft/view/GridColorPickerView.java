package com.obnsoft.view;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GridColorPickerView extends GridView
        implements android.widget.AdapterView.OnItemClickListener {

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public class MyAdapter extends ArrayAdapter<Integer> {

        class ViewHolder {
            TextView    tv;
            ImageView   iv;
        }

        private Context mContext;
        private int mChoice = -1;
        private ImageView[] mRadios;

        public MyAdapter(Context context, Integer[] objects) {
            super(context, 0, objects);
            mContext = context;
            mRadios = new ImageView[objects.length];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                RelativeLayout rl = new RelativeLayout(mContext);
                rl.setGravity(Gravity.CENTER);
                int pad = mBoxSize / 8;
                rl.setPadding(pad, pad, pad, pad);
                TextView tv = new TextView(mContext);
                ImageView iv = new ImageView(mContext);
                rl.addView(tv);
                rl.addView(iv);
                convertView = rl;
                holder = new ViewHolder();
                holder.tv = tv;
                holder.iv = iv;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            int color = getItem(position);
            holder.tv.setWidth(mBoxSize);
            holder.tv.setHeight(mBoxSize);
            holder.tv.setBackgroundColor(color);
            if (mRadios[position] == null) {
                mRadios[position] = holder.iv;
            }
            return convertView;
        }

        public int getChoice() {
            return mChoice;
        }

        public void setChoice(int position) {
            for (int i = 0; i < getCount(); i++) {
                if (mRadios[i] != null) {
                    mRadios[i].setImageResource(getChoiceDrawable(i == position));
                }
            }
            mChoice = position;
        }

        private int getChoiceDrawable(boolean b) {
            return b ? android.R.drawable.radiobutton_on_background :
                android.R.drawable.radiobutton_off_background;
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
        MyAdapter adapter = (MyAdapter) getAdapter();
        mCurColor = color;
        for (int i = 0; i < sPresets.length; i++) {
            if ((color | 0xFF000000) == sPresets[i]) {
                adapter.setChoice(i);
                return;
            }
        }
        adapter.setChoice(-1);
    }

    public int getColor() {
        return mCurColor;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MyAdapter adapter = (MyAdapter) parent.getAdapter();
        adapter.setChoice(position);
        mCurColor = sPresets[position];
        if (mListener != null) {
            mListener.colorChanged(mCurColor);
        }
    }

}
