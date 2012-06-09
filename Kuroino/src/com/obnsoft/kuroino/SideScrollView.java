package com.obnsoft.kuroino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class SideScrollView extends ScrollView {

    private SheetData mData = null;
    private SideView mChild = null;

    /*----------------------------------------------------------------------*/

    class SideView extends View {

        private View mParent = null;
        private Paint mPaintGrid = new Paint();
        private Paint mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean mIsFocus;
        private int mFocusRow = -1;
        private Bitmap mBmpCache = null;
        private Rect mRectTmp = new Rect();

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
                    mIsFocus = true;
                    mFocusRow = (int) event.getY() / mData.cellSize;
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
                    mFocusRow = -1;
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
                int scrollY = mParent.getScrollY();
                int scrollHeight = mParent.getHeight();
                mRectTmp.set(0, scrollY, getWidth(), scrollY + scrollHeight);
                c.drawBitmap(mBmpCache, mRectTmp, mRectTmp, null);

                if (mFocusRow >= 0) {
                    int cellSize = mData.cellSize;
                    mPaintGrid.setColor(mIsFocus ?
                            Color.argb(63, 255, 255, 0) : Color.argb(31, 255, 255, 0));
                    c.drawRect(0, mFocusRow * cellSize,
                            getWidth(), (mFocusRow + 1) * cellSize, mPaintGrid);
                }
            }
        }

        private void resize() {
            int width = mParent.getWidth();
            int height = 1;
            if (mData != null) {
                height = mData.names.size() * mData.cellSize + 1;
                createCache();
            }
            setMeasuredDimension(width, height);
        }

        private void createCache() {
            if (mBmpCache != null) {
                mBmpCache.recycle();
            }
            int width = mParent.getWidth();
            int cellSize = mData.cellSize;
            int rows = mData.names.size();
            if (width > 0) {
                mBmpCache = Bitmap.createBitmap(
                        width, rows * cellSize + 1, Bitmap.Config.RGB_565);
                Canvas c = new Canvas(mBmpCache);
                c.drawRGB(63, 63, 63);
                mPaintText.setTextSize(cellSize / 3f);
                FontMetrics fm = mPaintText.getFontMetrics();
                float tx = cellSize * 0.125f;
                float ty = (cellSize - fm.ascent - fm.descent) / 2f;
                mPaintGrid.setColor(Color.WHITE);
                for (int i = 0; i < rows; i++) {
                    int y = i * cellSize;
                    c.drawText(mData.names.get(i), tx, y + ty, mPaintText);
                    c.drawLine(0, y, width, y, mPaintGrid);
                }
                c.drawLine(0, rows * cellSize, width, rows * cellSize, mPaintGrid);
            } else {
                mBmpCache = null;
            }
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
