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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class SideScrollView extends ScrollView {

    private static final int[] ROW_COLORS = {0xFF1F271F, 0xFF1F2F1F};

    private SheetData mData = null;
    private SideView mChild = null;
    private int mFocusRow = SheetData.POS_GONE;
    private int mClickRow = SheetData.POS_GONE;
    private boolean mReserveScroll = false;

    /*----------------------------------------------------------------------*/

    class SideView extends View implements OnLongClickListener {

        private View mParent = null;
        private Paint mPaintGrid = new Paint();
        private Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);

        public SideView(View parent) {
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
                    mClickRow = mData.getRowByCoord(event.getY());
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mClickRow >= 0) {
                    ((MainActivity) getContext()).handleClick(
                            mParent, mClickRow, SheetData.POS_KEEP, (mClickRow == mFocusRow));
                    mFocusRow = mClickRow;
                    mClickRow = SheetData.POS_GONE;
                    postInvalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mClickRow >= 0) {
                    mClickRow = SheetData.POS_GONE;
                    postInvalidate();
                }
                break;
            }
            //return (mClickRow >= 0) ? true : super.onTouchEvent(event);
            return super.onTouchEvent(event);
        }

        @Override
        public boolean onLongClick(View v) {
            if (mClickRow >= 0) {
                ((MainActivity) getContext()).handleClick(
                        mParent, mClickRow, SheetData.POS_KEEP, true);
                mClickRow = SheetData.POS_GONE;
                postInvalidate();
                return true;
            }
            return false;
        }

        @Override
        public void onDraw(Canvas c) {
            if (mData != null) {
                int width = mParent.getWidth();
                int cellSize = mData.cellSize;
                int rows = mData.entries.size();

                int scrollY = mParent.getScrollY();
                int scrollHeight = mParent.getHeight();

                int startRow = Math.max(scrollY / cellSize, 0);
                int endRow = Math.min((scrollY + scrollHeight - 1) / cellSize, rows - 1);

                mPaintText.setTextSize(cellSize / 3f);
                FontMetrics fm = mPaintText.getFontMetrics();
                float tx = cellSize * 0.125f;
                float ty = (cellSize - fm.ascent - fm.descent) / 2f;
                for (int row = startRow; row <= endRow; row++) {
                    int y = row * cellSize;

                    /*  Background  */
                    mPaintGrid.setColor(ROW_COLORS[row & 1]);
                    c.drawRect(0, y, width, y + cellSize, mPaintGrid);

                    /*  Name  */
                    c.drawText(mData.entries.get(row).name, tx, y + ty, mPaintText);
                }

                /*  Grid  */
                mPaintGrid.setColor(Color.WHITE);
                for (int row = startRow; row <= endRow + 1; row++) {
                    c.drawLine(0, row * cellSize, width, row * cellSize, mPaintGrid);
                }

                /*  Highlight */
                if (mFocusRow >= 0) {
                    mPaintGrid.setColor(mData.focusColor);
                    c.drawRect(0, mFocusRow * cellSize,
                            getWidth(), (mFocusRow + 1) * cellSize, mPaintGrid);
                }
                if (mClickRow >= 0) {
                    mPaintGrid.setColor(mData.clickColor);
                    c.drawRect(0, mClickRow * cellSize,
                            getWidth(), (mClickRow + 1) * cellSize, mPaintGrid);
                }
            }
        }

        private void resize() {
            int width = mParent.getWidth();
            int height = (mData != null) ? mData.entries.size() * mData.cellSize + 1 : 1;
            setMeasuredDimension(width, height);
        }
    }

    /*----------------------------------------------------------------------*/

    public SideScrollView(Context context) {
        super(context);
        initialize();
    }
    public SideScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }
    public SideScrollView(Context context, AttributeSet attrs, int defStyle) {
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

    public void setFocus(int row, boolean scroll) {
        if (row != SheetData.POS_KEEP) {
            mFocusRow = row;
            mChild.postInvalidate();
            if (scroll && mFocusRow >= 0) {
                if (isLayoutRequested()) {
                    mReserveScroll = true;
                } else {
                    flyToFocusRow();
                }
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mReserveScroll) {
            flyToFocusRow();
            mReserveScroll = false;
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        ((MainActivity) getContext()).handleScroll(this, l, t);
    }

    private void initialize() {
        setVerticalScrollBarEnabled(false);
        mChild = new SideView(this);
        addView(mChild);
    }

    private void flyToFocusRow() {
        if (mData != null) {
            int h = getHeight();
            int y = getScrollY();
            int edge = getVerticalFadingEdgeLength();
            int y1 = Math.min(mFocusRow * mData.cellSize - edge, mChild.getHeight() - h);
            int y2 = Math.max((mFocusRow + 1) * mData.cellSize - h + edge, 0);
            if (y > y1 || y < y2) {
                smoothScrollTo(0, (Math.abs(y1 - y) < Math.abs(y2 - y)) ? y1 : y2);
            }
        }
    }
}
