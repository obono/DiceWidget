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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class HSVColorPickerView extends View implements ColorPickerInterface {

    private enum Target {
        NONE, HUE, SAT_VAL
    };

    private float[] mHSV = new float[3];
    private int     mCurColor;
    private int     mMinimumSize;
    private float   mRadius;
    private float   mSatValX;
    private float   mSatValY;
    private double  mLastHueDeg;
    private float   mSatValGapX;
    private float   mSatValGapY;
    private Target  mTarget = Target.NONE;
    private RectF   mRectHue = new RectF();
    private Paint   mPaintHue = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path    mPathSatVal = new Path();
    private Paint   mPaintSat = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint   mPaintVal = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF   mRectCurCol = new RectF();
    private Paint   mPaintCurCol = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint   mPaintCurColF = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint   mPaintCurColH = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnColorChangedListener mListener;

    private static final int[] HUE_COLORS = {
        0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
    };
    private static final int[] HUE_COLORS_T = {
        0x40FF0000, 0x40FF00FF, 0x400000FF, 0x4000FFFF, 0x4000FF00, 0x40FFFF00, 0x40FF0000
    };
    private static final SweepGradient HUE_GRADIENT =
        new SweepGradient(0, 0, HUE_COLORS, null);
    private static final SweepGradient HUE_GRADIENT_T =
        new SweepGradient(0, 0, HUE_COLORS_T, null);

    private static final float INTER_RATIO = 3f / 4f;
    private static final double SQRT3 = Math.sqrt(3.0);

    public HSVColorPickerView(Context context) {
        this(context, null);
    }

    public HSVColorPickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSVColorPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        mMinimumSize = Math.min(disp.getWidth(), disp.getHeight()) / 2;

        mPaintHue.setStyle(Paint.Style.STROKE);
        mPaintHue.setShader(HUE_GRADIENT);

        mPaintSat.setStyle(Paint.Style.FILL);
        mPaintVal.setStyle(Paint.Style.FILL);

        mPaintCurCol.setStyle(Paint.Style.FILL);
        mPaintCurColF.setStyle(Paint.Style.STROKE);
        mPaintCurColH.setStyle(Paint.Style.STROKE);

        setColor(Color.BLACK);
    }

    @Override
    public void setColor(int color) {
        mCurColor = color | 0xFF000000;
        float hue = mHSV[0];
        Color.colorToHSV(color, mHSV);
        if (mHSV[1] == 0f) {
            mHSV[0] = hue;
        }
        setPaintSatVal((double) mHSV[0]);
        setPaintCurColor(mHSV, false);
        postInvalidate();
    }

    @Override
    public int getColor() {
        return mCurColor;
    }

    @Override
    public void setListener(OnColorChangedListener l) {
        mListener = l;
    }

    public float[] getHSV() {
        return mHSV.clone();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(widthMeasureSpec) : mMinimumSize;
        int height = (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) ?
                MeasureSpec.getSize(heightMeasureSpec) : mMinimumSize;

        mRadius = Math.min(width, height) / 2f;
        float r = mRadius * ((INTER_RATIO + 1f) / 2f);
        mRectHue.set(-r, -r, r, r);
        mPaintHue.setStrokeWidth(mRadius * (1f - INTER_RATIO));
        setPaintSatVal((double) mHSV[0]);
        setPaintCurColor(mHSV, false);
        mPaintCurColF.setStrokeWidth(mRadius / 120f + 1f);
        mPaintCurColH.setStrokeWidth(mRadius / 120f + 1f);

        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - getWidth() / 2f;
        float y = event.getY() - getHeight() / 2f;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mTarget == Target.NONE) {
                    if (calcSatVal(x, y, true)) {
                        mTarget = Target.SAT_VAL;
                        mPaintHue.setShader(HUE_GRADIENT_T);
                        invalidate();
                    } else if (calcHue(x, y, true)) {
                        mTarget = Target.HUE;
                        setPaintSatVal(mHSV[0]);
                        invalidate();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mTarget == Target.HUE) {
                    calcHue(x, y, false);
                    invalidate();
                } else if (mTarget == Target.SAT_VAL) {
                    calcSatVal(x, y, false);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                mTarget = Target.NONE;
                mPaintHue.setShader(HUE_GRADIENT);
                setPaintSatVal(mHSV[0]);
                invalidate();
                break;
        }
        return true;
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        canvas.translate(getWidth() / 2f, getHeight() / 2f);
        canvas.drawOval(mRectHue, mPaintHue);
        double d = Math.toRadians(mHSV[0]);
        float nx = (float) Math.cos(d);
        float ny = (float) -Math.sin(d);
        float nr = mRadius * INTER_RATIO;
        canvas.drawLine(nx * mRadius, ny * mRadius, nx * nr, ny * nr, mPaintCurColH);
        canvas.drawPath(mPathSatVal, mPaintVal);
        canvas.drawPath(mPathSatVal, mPaintSat);
        canvas.drawOval(mRectCurCol, mPaintCurCol);
        canvas.drawOval(mRectCurCol, mPaintCurColF);
    }

    public static double calcBrightness(int color) {
        return (Color.green(color) * 6.0 + Color.red(color) * 3.0 + Color.blue(color)) / 2295.0;
    }

    private boolean calcHue(float x, float y, boolean isDown) {
        double deg = Math.toDegrees(Math.atan2(-y, x));
        if (isDown) {
            if (Math.sqrt(x * x + y * y) > (double) mRadius * 1.25) {
                return false;
            }
        } else {
            double hue = mHSV[0] + deg - mLastHueDeg;
            if (hue < 0.0)   hue += 360.0;
            if (hue > 360.0) hue -= 360.0;
            mHSV[0] = (float) hue;
            setPaintSatVal(hue);
            setPaintCurColor(mHSV, true);
        }
        mLastHueDeg = deg;
        return true;
    }

    private boolean calcSatVal(float x, float y, boolean isDown) {
        double r = mRadius * INTER_RATIO;
        if (isDown) {
            if (Math.sqrt(x * x + y * y) > r) {
                return false;
            }
            mSatValGapX = mSatValX - x;
            mSatValGapY = mSatValY - y;
        } else {
            x += mSatValGapX;
            y += mSatValGapY;
            double d = Math.toRadians(mHSV[0] + 120.0);
            double nx = Math.cos(d);
            double ny = -Math.sin(d);
            double val = (2.0 - (nx * x + ny * y) * 2.0 / r) / 3.0;
            double sat = (val > 0.0) ?
                    0.5 + (-ny * x + nx * y) / r / SQRT3 / val : 0.0;
            if (val > 1.0) val = 1.0;
            if (val < 0.0) val = 0.0;
            if (sat > 1.0) sat = 1.0;
            if (sat < 0.0) sat = 0.0;
            mHSV[1] = (float) sat;
            mHSV[2] = (float) val;
            setPaintCurColor(mHSV, true);
        }
        return true;
    }

    private void setPaintSatVal(double hue) {
        double r = mRadius * INTER_RATIO;
        float x[] = new float[3];
        float y[] = new float[3];
        for (int i = 0; i < 3; i++) {
            double d = Math.toRadians(hue + i * 120.0);
            x[i] = (float) (Math.cos(d) * r);
            y[i] = (float) (-Math.sin(d) * r);
        }
        mPathSatVal.reset();
        mPathSatVal.moveTo(x[0], y[0]);
        mPathSatVal.lineTo(x[1], y[1]);
        mPathSatVal.lineTo(x[2], y[2]);
        mPathSatVal.lineTo(x[0], y[0]);

        int col[] = {Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK};
        float pos[] = new float[4];
        pos[0] = 0f;
        pos[3] = 1f;
        if (hue < 150.0) {
            pos[1] = (float) ((150.0 - hue) / 360.0);
            pos[2] = (float) ((210.0 - hue) / 360.0);
        } else if (hue > 210.0) {
            pos[1] = (float) ((510.0 - hue) / 360.0);
            pos[2] = (float) ((570.0 - hue) / 360.0);
        } else {
            col[0] = col[3] = 0x010101 * (int) ((210.0 - hue) * 256.0 / 60.0) | 0xFF000000;
            col[1] = Color.BLACK;
            col[2] = Color.WHITE;
            pos[1] = (float) ((210.0 - hue) / 360.0);
            pos[2] = (float) ((510.0 - hue) / 360.0);
        }
        int pureCol = Color.HSVToColor(new float[] {(float) hue, 1f, 1f});
        if (mTarget == Target.HUE) {
            for (int i = 0; i < 4; i++) {
                col[i] &= 0x40FFFFFF;
            }
            pureCol &= 0x40FFFFFF;
        }
        mPaintVal.setShader(new SweepGradient(x[0], y[0], col, pos));
        mPaintSat.setShader(new LinearGradient(x[0], y[0], (x[1] + x[2]) / 2f, (y[1] + y[2]) / 2f,
                pureCol, pureCol & 0xFFFFFF, Shader.TileMode.CLAMP));
        mPaintCurColH.setColor((calcBrightness(pureCol) < 0.5) ? Color.WHITE : Color.BLACK);
    }

    private void setPaintCurColor(float[] hsv, boolean updated) {
        if (updated) {
            while (hsv[0] < 0f)     hsv[0] += 360f;
            while (hsv[0] >= 360f)  hsv[0] -= 360f;
            if (hsv[1] > 1f) hsv[1] = 1f;
            if (hsv[1] < 0f) hsv[1] = 0f;
            if (hsv[2] > 1f) hsv[2] = 1f;
            if (hsv[2] < 0f) hsv[2] = 0f;
            mCurColor = Color.HSVToColor(hsv);
        }
        mPaintCurCol.setColor(mCurColor);
        mPaintCurColF.setColor((calcBrightness(mCurColor) < 0.5) ? Color.WHITE : Color.BLACK);

        double r = mRadius * INTER_RATIO;
        double d = Math.toRadians(hsv[0] + 120.0);
        double dx = Math.cos(d) * r;
        double dy = -Math.sin(d) * r;
        mSatValX = (float) (dx * (1.0 - hsv[2] * 1.5) - dy * (hsv[1] - 0.5) * hsv[2] * SQRT3);
        mSatValY = (float) (dy * (1.0 - hsv[2] * 1.5) + dx * (hsv[1] - 0.5) * hsv[2] * SQRT3);
        float r8 = mRadius / 8f;
        mRectCurCol.set(mSatValX - r8, mSatValY - r8, mSatValX + r8, mSatValY + r8);

        if (mListener != null) {
            mListener.colorChanged(mCurColor);
        }
    }
}
