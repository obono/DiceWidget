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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ScrollView;

public class SideScrollView extends ScrollView {

    private SheetData mData = null;
    private SideView mChild = null;
    private int mFocusRow = -1;

    /*----------------------------------------------------------------------*/

    class SideView extends View {

        private View mParent = null;
        private Paint mPaintGrid = new Paint();
        private Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean mIsFocus;

        public SideView(View parent) {
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
                    int row = (int) event.getY() / mData.cellSize;
                    if (row >= 0 && row < mData.entries.size()) {
                        mIsFocus = true;
                        mFocusRow = (int) event.getY() / mData.cellSize;
                        ((MainActivity) getContext()).handleFocus(mParent, mFocusRow, -1);
                        postInvalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsFocus) {
                    ((MainActivity) getContext()).handleClick(mParent, mFocusRow, -1);
                    postInvalidate();
                    mIsFocus = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsFocus) {
                    mFocusRow = -1;
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
                int width = mParent.getWidth();
                int cellSize = mData.cellSize;
                int rows = mData.entries.size();

                int scrollY = mParent.getScrollY();
                int scrollHeight = mParent.getHeight();

                int startRow = Math.max(scrollY / cellSize, 0);
                int endRow = Math.min((scrollY + scrollHeight - 1) / cellSize, rows - 1);

                TypedArray rowsCols = getResources().obtainTypedArray(R.array.rows_colors);
                mPaintText.setTextSize(cellSize / 3f);
                FontMetrics fm = mPaintText.getFontMetrics();
                float tx = cellSize * 0.125f;
                float ty = (cellSize - fm.ascent - fm.descent) / 2f;
                for (int row = startRow; row <= endRow; row++) {
                    int y = row * cellSize;

                    /*  Background  */
                    mPaintGrid.setColor(rowsCols.getColor(row & 1, Color.TRANSPARENT));
                    c.drawRect(0, y, width, y + cellSize, mPaintGrid);

                    /*  Name  */
                    c.drawText(mData.entries.get(row).name, tx, y + ty, mPaintText);
                }

                /*  Grid  */
                mPaintGrid.setColor(Color.WHITE);
                for (int row = startRow; row <= endRow + 1; row++) {
                    c.drawLine(0, row * cellSize, width, row * cellSize, mPaintGrid);
                }

                /*  Focus  */
                if (mFocusRow >= 0) {
                    mPaintGrid.setColor(mIsFocus ?
                            Color.argb(63, 255, 255, 0) : Color.argb(31, 255, 255, 0));
                    c.drawRect(0, mFocusRow * cellSize,
                            getWidth(), (mFocusRow + 1) * cellSize, mPaintGrid);
                }
            }
        }

        private void resize() {
            int width = mParent.getWidth();
            int height = (mData != null) ? mData.entries.size() * mData.cellSize + 1 : 1;
            setMeasuredDimension(width, height);
            layout(0, 0, width, height);
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

    public void setData(SheetData data) {
        mData = data;
        mChild.resize();
        postInvalidate();
    }

    public void setFocus(int row) {
        mFocusRow = row;
        mChild.postInvalidate();
    }

    @Override
    protected ContextMenuInfo getContextMenuInfo() {
        return new AdapterContextMenuInfo(null, mFocusRow, 0);
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
}
