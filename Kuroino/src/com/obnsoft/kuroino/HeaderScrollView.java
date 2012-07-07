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
    private static final int[] DWEEK_COLORS = {
        0xFF2F1F1F, 0xFF1F1F1F, 0xFF1F1F1F, 0xFF1F1F1F, 0xFF1F1F1F, 0xFF1F1F1F, 0xFF1F1F3F
    };

    private SheetData mData = null;
    private HeaderView mChild = null;
    private int mFocusCol = SheetData.POS_GONE;
    private int mClickCol = SheetData.POS_GONE;
    private boolean mReserveScroll = false;

    /*----------------------------------------------------------------------*/

    class HeaderView extends View implements OnLongClickListener {

        private View mParent = null;
        private Paint mPaintGrid = new Paint();
        private Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        private String[] mMonthStrs = getResources().getStringArray(R.array.month_strings);
        private String[] mDweekStrs = getResources().getStringArray(R.array.dweek_strings);

        public HeaderView(View parent) {
            super(parent.getContext());
            mParent = parent;
            mPaintGrid.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaintText.setColor(Color.LTGRAY);
            setOnLongClickListener(this);
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
                    ((MainActivity) getContext()).handleMouseDown(mParent);
                    mClickCol = mData.getColumnByCoord(event.getX());
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mClickCol >= 0) {
                    ((MainActivity) getContext()).handleClick(
                            mParent, SheetData.POS_KEEP, mClickCol, (mClickCol == mFocusCol));
                    mFocusCol = mClickCol;
                    mClickCol = SheetData.POS_GONE;
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mClickCol >= 0) {
                    mClickCol = SheetData.POS_GONE;
                    postInvalidate();
                }
                break;
            }
            //return (mClickCol >= 0) ? true : super.onTouchEvent(event);
            return super.onTouchEvent(event);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mClickCol >= 0) {
                ((MainActivity) getContext()).handleClick(
                        mParent, SheetData.POS_KEEP, mClickCol, true);
                mClickCol = SheetData.POS_GONE;
                postInvalidate();
                return true;
            }
            return false;
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

                /*  Background  */
                mPaintGrid.setColor(DWEEK_COLORS[1]);
                c.drawRect(scrollX, 0, scrollX + scrollWidth, height, mPaintGrid);

                int lastYear = 0;
                int lastMonth = 0;
                int lastDatePos = -1;
                FontMetrics fm;
                String str;
                float strWidth;

                for (int col = startCol; col <= endCol; col++) {
                    Calendar cal = mData.dates.get(col);
                    int year = cal.get(Calendar.YEAR);
                    int month = cal.get(Calendar.MONTH);
                    boolean isNewMonth = (year != lastYear || month != lastMonth);
                    int dweek = cal.get(Calendar.DAY_OF_WEEK) - 1;
                    int x = col * cellSize;

                    /*  Background  */
                    mPaintText.setTextSize(height / 4f);
                    fm = mPaintText.getFontMetrics();
                    float y = fm.descent - fm.ascent;
                    mPaintGrid.setColor(DWEEK_COLORS[dweek]);
                    c.drawRect(x, y, x + cellSize, height, mPaintGrid);

                    /*  Grid  */
                    mPaintGrid.setColor(Color.WHITE);
                    c.drawLine(x, isNewMonth ? 0 : y, x, height, mPaintGrid);

                    /*  Day of week  */
                    mPaintText.setTextSize(height / 4f);
                    fm = mPaintText.getFontMetrics();
                    str = mDweekStrs[dweek];
                    strWidth = mPaintText.measureText(str);
                    c.drawText(str, x + (cellSize - strWidth) / 2f, 
                            height - fm.descent, mPaintText);

                    /*  Month and year  */
                    if (isNewMonth) {
                        if (lastDatePos >= 0) {
                            str = String.format(MONTH_YEAR_FORMAT,
                                    mMonthStrs[lastMonth], lastYear % 100);
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

                    /*  Day of month  */
                    mPaintText.setTextSize(height / 2f);
                    fm = mPaintText.getFontMetrics();
                    str = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
                    strWidth = mPaintText.measureText(str);
                    c.drawText(str, x + (cellSize - strWidth) / 2f,
                            (height - fm.ascent - fm.descent) / 2f, mPaintText);
                }

                /*  Final grid  */
                if (endCol == cols - 1) {
                    mPaintGrid.setColor(Color.WHITE);
                    c.drawLine(cols * cellSize, 0, cols * cellSize, height, mPaintGrid);
                }

                /*  Final month and Year  */
                if (lastDatePos >= 0) {
                    mPaintText.setTextSize(height / 4f);
                    fm = mPaintText.getFontMetrics();
                    str = String.format(MONTH_YEAR_FORMAT,
                            mMonthStrs[lastMonth], lastYear % 100);
                    strWidth = mPaintText.measureText(str);
                    float tx = lastDatePos * cellSize + (cellSize - strWidth) / 2;
                    float cx = scrollX + (baseWidth - strWidth) / 2f;
                    c.drawText(str, Math.max(cx, tx), -fm.ascent, mPaintText);
                }

                /*  Highlight  */
                if (mFocusCol >= 0) {
                    mPaintGrid.setColor(SheetData.FOCUS_COLOR);
                    c.drawRect(mFocusCol * cellSize, 0,
                            (mFocusCol + 1) * cellSize, getHeight(), mPaintGrid);
                }
                if (mClickCol >= 0) {
                    mPaintGrid.setColor(SheetData.CLICK_COLOR);
                    c.drawRect(mClickCol * cellSize, 0,
                            (mClickCol + 1) * cellSize, getHeight(), mPaintGrid);
                }
            }
        }

        private void resize() {
            int width = (mData != null) ?  mData.dates.size() * mData.cellSize + 1 : 1;
            int height = mParent.getHeight();
            setMeasuredDimension(width, height);
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

    public void setSheetData(SheetData data) {
        mData = data;
    }

    public void refreshView() {
        mChild.resize();
        requestLayout();
        postInvalidate();
    }

    public void setFocus(int col, boolean scroll) {
        if (col != SheetData.POS_KEEP) {
            mFocusCol = col;
            mChild.postInvalidate();
            if (scroll && mFocusCol >= 0) {
                if (isLayoutRequested()) {
                    mReserveScroll = true;
                } else {
                    flyToFocusColumn();
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mReserveScroll) {
            flyToFocusColumn();
            mReserveScroll = false;
        }
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

    private void flyToFocusColumn() {
        if (mData != null) {
            int w = getWidth();
            int x = getScrollX();
            int edge = getHorizontalFadingEdgeLength();
            int x1 = Math.min(mFocusCol * mData.cellSize - edge, mChild.getWidth() - w);
            int x2 = Math.max((mFocusCol + 1) * mData.cellSize - w + edge, 0);
            if (x > x1 || x < x2) {
                smoothScrollTo((Math.abs(x1 - x) < Math.abs(x2 - x)) ? x1 : x2, 0);
            }
        }
    }

}
