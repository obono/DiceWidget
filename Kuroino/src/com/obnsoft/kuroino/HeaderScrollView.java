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

package com.obnsoft.kuroino;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class HeaderScrollView extends HorizontalScrollView {

    private static final String[] MONTHS = {
        //"Jan.", "Feb.", "Mar.", "Apr.", "May.", "Jun.",
        //"Jul.", "Aug.", "Sep.", "Oct.", "Nov.", "Dec.",
        "1ŒŽ", "2ŒŽ", "3ŒŽ", "4ŒŽ", "5ŒŽ", "6ŒŽ",
        "7ŒŽ", "8ŒŽ", "9ŒŽ", "10ŒŽ", "11ŒŽ", "12ŒŽ",
    };

    private static final String[] DWEEK = {
        //"Sun.", "Mon.", "Tue.", "Wed.", "Thu.", "Fri.", "Sat.",
        "“ú", "ŒŽ", "‰Î", "…", "–Ø", "‹à", "“y",
    };

    private SheetData mData = null;
    private HeaderView mChild = null;

    /*----------------------------------------------------------------------*/

    class HeaderView extends View {

        private View mParent = null;
        private boolean mIsFocus;
        private int mFocusCol = -1;
        private Paint mPaintGrid = new Paint();
        private Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Bitmap mBmpCache = null;
        private Rect mRectTmp = new Rect();

        public HeaderView(View parent) {
            super(parent.getContext());
            mParent = parent;
            mPaintGrid.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaintGrid.setColor(Color.WHITE);
            mPaintText.setColor(Color.LTGRAY);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            resize();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mData != null) {
                    mIsFocus = true;
                    mFocusCol = (int) event.getX() / mData.cellSize;
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsFocus) {
                    postInvalidate();
                    mIsFocus = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsFocus) {
                    mFocusCol = -1;
                    postInvalidate();
                    mIsFocus = false;
                }
                break;
            }
            return (mIsFocus) ? true : super.onTouchEvent(event);
        }

        @Override
        public void onDraw(Canvas c) {
            if (mData != null && mBmpCache != null) {
                int scrollX = mParent.getScrollX();
                int scrollWidth = mParent.getWidth();
                mRectTmp.set(scrollX, 0, scrollX + scrollWidth, getHeight());
                c.drawBitmap(mBmpCache, mRectTmp, mRectTmp, null);

                if (mFocusCol >= 0) {
                    int cellSize = mData.cellSize;
                    mPaintGrid.setColor(mIsFocus ?
                            Color.argb(63, 255, 255, 0) : Color.argb(31, 255, 255, 0));
                    c.drawRect(mFocusCol * cellSize, 0,
                            (mFocusCol + 1) * cellSize, getHeight(), mPaintGrid);
                }
            }
        }

        private void resize() {
            int width = 1;
            int height = mParent.getHeight();
            if (mData != null) {
                width = mData.dates.size() * mData.cellSize + 1;
                createCache();
            }
            setMeasuredDimension(width, height);
        }

        private void createCache() {
            if (mBmpCache != null) {
                mBmpCache.recycle();
            }
            int height = mParent.getHeight();
            int cellSize = mData.cellSize;
            int cols = mData.dates.size();
            if (height > 0) {
                mBmpCache = Bitmap.createBitmap(
                        cols * cellSize + 1, height, Bitmap.Config.RGB_565);
                Canvas c = new Canvas(mBmpCache);
                c.drawRGB(31, 31, 31);

                int lastYear = 0;
                int lastMonth = 0;
                String str;
                float strWidth;
                for (int i = 0; i < cols; i++) {
                    int x = i * cellSize;
                    Calendar cal = mData.dates.get(i);
                    int dweek = cal.get(Calendar.DAY_OF_WEEK);
                    if (dweek == Calendar.SUNDAY) {
                        mPaintGrid.setColor(Color.rgb(47, 31, 31));
                        c.drawRect(i * cellSize, 0, (i + 1) * cellSize, height, mPaintGrid);
                        mPaintGrid.setColor(Color.WHITE);
                    }
                    if (dweek == Calendar.SATURDAY) {
                        mPaintGrid.setColor(Color.rgb(31, 31, 63));
                        c.drawRect(i * cellSize, 0, (i + 1) * cellSize, height, mPaintGrid);
                        mPaintGrid.setColor(Color.WHITE);
                    }

                    mPaintText.setTextSize(height / 4f);
                    FontMetrics fm = mPaintText.getFontMetrics();

                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    if (year != lastYear || month != lastMonth) {
                        str = String.format("%s '%02d", MONTHS[month], year % 100);
                        strWidth = mPaintText.measureText(str);
                        c.drawText(str, x + (cellSize - strWidth) / 2, -fm.ascent, mPaintText);
                        lastYear = year;
                        lastMonth = month;
                    }

                    str = DWEEK[cal.get(Calendar.DAY_OF_WEEK) - 1];
                    strWidth = mPaintText.measureText(str);
                    c.drawText(str, x + (cellSize - strWidth) / 2f,
                            height - fm.descent, mPaintText);

                    mPaintText.setTextSize(height / 2f);
                    fm = mPaintText.getFontMetrics();
                    str = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                    strWidth = mPaintText.measureText(str);
                    c.drawText(str, x + (cellSize - strWidth) / 2f,
                            (height - fm.ascent - fm.descent) / 2f, mPaintText);
                    c.drawLine(x, 0, x, height, mPaintGrid);
                }
                c.drawLine(cols * cellSize, 0, cols * cellSize, height, mPaintGrid);
            } else {
                mBmpCache = null;
            }
        }

    }

    /*----------------------------------------------------------------------*/

    public HeaderScrollView(Context context) {
        super(context);
        initialize();
    }
    public HeaderScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    public HeaderScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }

    public void setData(SheetData data) {
        mData = data;
        mChild.resize();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        ((MainActivity) getContext()).handleScroll(this, l, t);
    }

    private void initialize() {
        setHorizontalScrollBarEnabled(false);
        mChild = new HeaderView(this);
        addView(mChild);
    }
}
