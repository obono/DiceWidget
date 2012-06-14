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

import java.util.GregorianCalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    HeaderScrollView    mHeader;
    SideScrollView      mSide;
    SheetScrollView     mSheet;
    SheetData           mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHeader = (HeaderScrollView) findViewById(R.id.view_head);
        mSide = (SideScrollView) findViewById(R.id.view_side);
        mSheet = (SheetScrollView) findViewById(R.id.view_main);

        mData = new SheetData();
        mData.cellSize = (int) (48f * getResources().getDisplayMetrics().density);
        mData.createNewData(new GregorianCalendar(2012, 0, 1),
                new GregorianCalendar(2012, 11, 31),
                0,
                new boolean[] {false, true, false, true, false, false, true},
                new String[] {"Australia", "Brazil", "Canada", "Denmark", "Egypt", "France", "German"});
        mHeader.setData(mData);
        mSide.setData(mData);
        mSheet.setData(mData);
    }

    protected void handleFocus(View v, int row, int col) {
        if (v != mHeader) {
            mHeader.setFocus(col);
            mHeader.fling(0);
        }
        if (v != mSide) {
            mSide.setFocus(row);
            mSide.fling(0);
        }
        if (v != mSheet) {
            mSheet.setFocus(row, col);
            mSheet.fling(0, 0);
        }
    }

    protected void handleClick(View v, int row, int col) {
        if (v == mHeader) {
            ; // TODO
        } else if (v == mSide) {
            ; // TODO
        } else if (v == mSheet) {
            String[] symbolStrs = getResources().getStringArray(R.array.symbol_strings);
            String[] attends = mData.attends.get(row);
            if (symbolStrs.length == 0 || attends.length - 1 < row) {
                return;
            }
            if (attends[col] == null) {
                attends[col] = symbolStrs[0];
            } else {
                boolean match = false;
                for (int i = 0; i < symbolStrs.length - 1; i++) {
                    if (symbolStrs[i].equals(attends[col])) {
                        attends[col] = symbolStrs[i + 1];
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    attends[col] = null;
                }
            }
        }
    }

    protected void handleScroll(View v, int l, int t) {
        if (v == mHeader) {
            mSheet.scrollTo(l, mSheet.getScrollY());
        } else if (v == mSide) {
            mSheet.scrollTo(mSheet.getScrollX(), t);
        } else if (v == mSheet) {
            mHeader.scrollTo(l, 0);
            mSide.scrollTo(0, t);
        }
    }
}