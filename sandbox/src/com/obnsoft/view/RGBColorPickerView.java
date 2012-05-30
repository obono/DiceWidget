package com.obnsoft.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class RGBColorPickerView extends LinearLayout implements ColorPickerInterface {

    private interface OnValueChangedListener {
        void onValueChanged(View view, int value);
    }

    class MeterScrollView extends HorizontalScrollView {

        private int mScale = 12;
        private int mHeight;
        private int mValue = 0;
        private int mMinValue = 0;
        private int mMaxValue = 100;
        private int mUnit = 10;
        private MeterView mChild;
        private OnValueChangedListener mListener;

        private class MeterView extends View {

            private MeterScrollView mParent;
            private Paint mPaint;

            public MeterView(Context context, MeterScrollView parent) {
                super(context);
                mParent = parent;
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            }

            protected void resize() {
                int width = (mMaxValue - mMinValue) * mScale + 1;
                int margin = mParent.getWidth() / 2;
                setMeasuredDimension(width + margin * 2, mHeight);
                mParent.setValue(mValue);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                resize();
            }

            @Override
            public void onDraw(Canvas c) {
                int margin = mParent.getWidth() / 2;
                int offset = mParent.getScrollX();
                int value = (offset + mScale / 2) / mScale + mMinValue;

                mPaint.setColor(Color.LTGRAY);
                mPaint.setTextSize(mHeight / 4);
                int iMax = Math.min(value  + margin / mScale + 1, mMaxValue);
                for (int i = Math.max(value - margin / mScale - 1, mMinValue); i <= iMax; i++) {
                    boolean isUnit = (i % mUnit == 0 || i == mMinValue || i == mMaxValue);
                    int x = margin + (i - mMinValue) * mScale;
                    c.drawLine(x, 0, x, mHeight / (isUnit ? 2 : 4), mPaint);
                    if (isUnit && Math.abs(offset + margin - x) > mHeight / 2) {
                        String s = String.valueOf(i);
                        c.drawText(s, x - mPaint.measureText(s) / 2, mHeight * 5 / 8, mPaint);
                    }
                }
                mPaint.setColor(Color.WHITE);
                mPaint.setTextSize(mHeight / 2);
                String s = String.valueOf(value);
                c.drawText(s, offset + margin - mPaint.measureText(s) / 2,
                        mHeight * 7 / 8, mPaint);
            }
        }

        public MeterScrollView(Context context) {
            super(context);
            mScale = (int) (getResources().getDisplayMetrics().density * 12f);
            mHeight = mScale * 4;
            mChild = new MeterView(context, this);
            addView(mChild);
            setHorizontalScrollBarEnabled(false);
        }

        public void setRange(int min, int max) {
            setRange(min, max, mUnit);
        }

        public void setRange(int min, int max, int unit) {
            mMinValue = min;
            mMaxValue = max;
            mUnit = unit;
            mChild.resize();
            setValue(mValue);
        }

        public void setScale(int scale) {
            mScale = scale;
            mChild.resize();
            setValue(mValue);
        }

        public void setValue(int value) {
            if (value < mMinValue) value = mMinValue;
            if (value > mMaxValue) value = mMaxValue;
            mValue = value;
            scrollTo((mValue - mMinValue) * mScale, 0);
        }

        public int getValue() {
            return mValue;
        }

        public void setValueChangedListener(OnValueChangedListener listener) {
            mListener = listener;
        }

        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            int value = (l + mScale / 2) / mScale + mMinValue;
            if (mValue != value) {
                mValue = value;
                if (mListener != null) {
                    mListener.onValueChanged(this, value);
                }
            }
        }
    }

    private int mCurColor;
    private int mMinimumSize;
    private MeterScrollView mRedMeter;
    private MeterScrollView mGreenMeter;
    private MeterScrollView mBlueMeter;
    private OnColorChangedListener mListener;

    public RGBColorPickerView(Context context) {
        this(context, null);
    }

    public RGBColorPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        mMinimumSize = Math.min(disp.getWidth(), disp.getHeight()) / 2;

        mRedMeter = new MeterScrollView(context);
        mRedMeter.setBackgroundDrawable(new ShapeDrawable(new RectShape()));
        mRedMeter.setRange(0, 255, 8);
        addView(mRedMeter);

        mGreenMeter = new MeterScrollView(context);
        mGreenMeter.setBackgroundDrawable(new ShapeDrawable(new RectShape()));
        mGreenMeter.setRange(0, 255, 8);
        addView(mGreenMeter);

        mBlueMeter = new MeterScrollView(context);
        mBlueMeter.setBackgroundDrawable(new ShapeDrawable(new RectShape()));
        mBlueMeter.setRange(0, 255, 8);
        addView(mBlueMeter);

        OnValueChangedListener listener = new OnValueChangedListener() {
            @Override
            public void onValueChanged(View view, int value) {
                mCurColor = Color.rgb(mRedMeter.getValue(),
                        mGreenMeter.getValue(), mBlueMeter.getValue());
                if (mListener != null) {
                    mListener.colorChanged(mCurColor);
                }
            }
        };
        mRedMeter.setValueChangedListener(listener);
        mGreenMeter.setValueChangedListener(listener);
        mBlueMeter.setValueChangedListener(listener);

        setColor(Color.BLACK);
    }

    @Override
    public void setColor(int color) {
        mCurColor = color | 0xFF000000;
        mRedMeter.setValue(Color.red(mCurColor));
        mGreenMeter.setValue(Color.green(mCurColor));
        mBlueMeter.setValue(Color.blue(mCurColor));
        if (mListener != null) {
            mListener.colorChanged(mCurColor);
        }
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

        ShapeDrawable sd;
        sd = (ShapeDrawable) mRedMeter.getBackground();
        sd.getPaint().setShader(new LinearGradient(0, 0, width / 2, 0,
                Color.BLACK, 0xFFC00000, Shader.TileMode.MIRROR));
        mRedMeter.setFadingEdgeLength(width / 3);
        sd = (ShapeDrawable) mGreenMeter.getBackground();
        sd.getPaint().setShader(new LinearGradient(0, 0, width / 2, 0,
                Color.BLACK, 0xFF008000, Shader.TileMode.MIRROR));
        mGreenMeter.setFadingEdgeLength(width / 3);
        sd = (ShapeDrawable) mBlueMeter.getBackground();
        sd.getPaint().setShader(new LinearGradient(0, 0, width / 2, 0,
                Color.BLACK, 0xFF0000FF, Shader.TileMode.MIRROR));
        mBlueMeter.setFadingEdgeLength(width / 3);

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                heightMeasureSpec);
    }
}
