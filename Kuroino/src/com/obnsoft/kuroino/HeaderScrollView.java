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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class HeaderScrollView extends HorizontalScrollView {

    private static final String MONTH_YEAR_FORMAT = "%s '%02d";

    private SheetData mData = null;
    private HeaderView mChild = null;
    private int mFocusCol = -1;

    /*----------------------------------------------------------------------*/

    class HeaderView extends View {

        private View mParent = null;
        private boolean mIsFocus;
        private Paint mPaintGrid = new Paint();
        private Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);

        public HeaderView(View parent) {
            super(parent.getContext());
            mParent = parent;
            mPaintGrid.setStyle(Paint.Style.FILL_AND_STROKE);
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
                    int col = (int) event.getX() / mData.cellSize;
                    if (mFocusCol >= 0 && mFocusCol < mData.dates.size()) {
                        mIsFocus = true;
                        mFocusCol = col;
                        ((MainActivity) getContext()).handleFocus(mParent, -1, mFocusCol);
                        postInvalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsFocus) {
                    ((MainActivity) getContext()).handleClick(mParent, -1, mFocusCol);
                    postInvalidate();
                    mIsFocus = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsFocus) {
                    mFocusCol = -1;
                    ((MainActivity) getContext()).handleFocus(mParent, -1, -1);
                    postInvalidate();
                    mIsFocus = false;
                }
                break;
            }
            return (mIsFocus) ? true : super.onTouchEvent(event);
        }

        @Override
        public void onDraw(Canvas c) {
            if (mData != null) {
                int height = mParent.getHeight();
                int cellSize = mData.cellSize;
                int cols = mData.dates.size();

                int scrollX = mParent.getScrollX();
                int scrollWidth = mParent.getWidth();
                int baseWidth = Math.min(mParent.getWidth(), getWidth());

                int startCol = Math.max(scrollX / cellSize, 0);
                int endCol = Math.min((scrollX + scrollWidth - 1) / cellSize, cols - 1);

                String[] monthStrs = getResources().getStringArray(R.array.month_strings);
                String[] dweekStrs = getResources().getStringArray(R.array.dweek_strings);
                TypedArray dweekCols = getResources().obtainTypedArray(R.array.dweek_colors);

                /*  Background  */
                mPaintGrid.setColor(dweekCols.getColor(1, Color.TRANSPARENT));
                c.drawRect(scrollX, 0, scrollX + scrollWidth, height, mPaintGrid);

                int lastYear = 0;
                int lastMonth = 0;
                int lastDatePos = -1;
                FontMetrics fm = mPaintText.getFontMetrics();
                String str;
                float strWidth;

                for (int col = startCol; col <= endCol; col++) {
                    Calendar cal = mData.dates.get(col);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    boolean isNewMonth = (year != lastYear || month != lastMonth);
                    int dweek = cal.get(Calendar.DAY_OF_WEEK) - 1;
                    int x = col * cellSize;
                    float y = fm.descent - fm.ascent;

                    /*  Background  */
                    mPaintGrid.setColor(dweekCols.getColor(dweek, Color.TRANSPARENT));
                    c.drawRect(x, y, x + cellSize, height, mPaintGrid);

                    /*  Grid  */
                    mPaintGrid.setColor(Color.WHITE);
                    c.drawLine(x, isNewMonth ? 0 : y, x, height, mPaintGrid);

                    /*  Day of month  */
                    mPaintText.setTextSize(height / 2f);
                    fm = mPaintText.getFontMetrics();
                    str = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                    strWidth = mPaintText.measureText(str);
                    c.drawText(str, x + (cellSize - strWidth) / 2f,
                            (height - fm.ascent - fm.descent) / 2f, mPaintText);

                    /*  Day of week  */
                    mPaintText.setTextSize(height / 4f);
                    fm = mPaintText.getFontMetrics();
                    str = dweekStrs[dweek];
                    strWidth = mPaintText.measureText(str);
                    c.drawText(str, x + (cellSize - strWidth) / 2f, 
                            height - fm.descent, mPaintText);

                    /*  Month and year  */
                    if (isNewMonth) {
                        if (lastDatePos >= 0) {
                            str = String.format(MONTH_YEAR_FORMAT,
                                    monthStrs[lastMonth], lastYear % 100);
                            strWidth = mPaintText.measureText(str);
                            float tx = (col - 1) * cellSize + (cellSize - strWidth) / 2;
                            float cx = scrollX + baseWidth / 2f;
                            if (lastDatePos * cellSize + cellSize / 2 <= cx &&
                                    (col - 1) * cellSize + cellSize / 2 >= cx) {
                                tx = cx - strWidth / 2f;
                            }
                            c.drawText(str, tx, -fm.ascent, mPaintText);
                        }
                        lastYear = year;
                        lastMonth = month;
                        lastDatePos = col;
                    }
                }

                /*  Final grid  */
                if (endCol == cols - 1) {
                    mPaintGrid.setColor(Color.WHITE);
                    c.drawLine(cols * cellSize, 0, cols * cellSize, height, mPaintGrid);
                }

                /*  Final month and Year  */
                if (lastDatePos >= 0) {
                    str = String.format(MONTH_YEAR_FORMAT,
                            monthStrs[lastMonth], lastYear % 100);
                    strWidth = mPaintText.measureText(str);
                    float tx = lastDatePos * cellSize + (cellSize - strWidth) / 2;
                    float cx = scrollX + (baseWidth - strWidth) / 2f;
                    c.drawText(str, Math.max(cx, tx), -fm.ascent, mPaintText);
                }

                /*  Focus  */
                if (mFocusCol >= 0) {
                    mPaintGrid.setColor(mIsFocus ?
                            Color.argb(63, 255, 255, 0) : Color.argb(31, 255, 255, 0));
                    c.drawRect(mFocusCol * cellSize, 0,
                            (mFocusCol + 1) * cellSize, getHeight(), mPaintGrid);
                }
            }
        }

        private void resize() {
            int width = (mData != null) ?  mData.dates.size() * mData.cellSize + 1 : 1;
            int height = mParent.getHeight();
            setMeasuredDimension(width, height);
            layout(0, 0, width, height);
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
        postInvalidate();
    }

    public void setFocus(int col) {
        mFocusCol = col;
        mChild.postInvalidate();
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
