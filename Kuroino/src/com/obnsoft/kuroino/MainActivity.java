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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

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
        setData(mData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.add(0, Menu.FIRST, Menu.NONE, "Import")
            .setIcon(android.R.drawable.ic_menu_set_as);
        menu.add(0, Menu.FIRST + 1, Menu.NONE, "Export")
            .setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, Menu.FIRST + 2, Menu.NONE, "Add date")
            .setIcon(android.R.drawable.ic_menu_day);
        menu.add(0, Menu.FIRST + 3, Menu.NONE, "Add member")
            .setIcon(android.R.drawable.ic_menu_add);
        return ret;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == 0) {
            String filePath = Environment.getExternalStorageDirectory() + "/hoge.csv";
            switch (item.getItemId()) {
            case Menu.FIRST:
                mData.importExternalData(filePath);
                setData(mData);
                return true;
            case Menu.FIRST + 1:
                mData.exportCurrentData(filePath);
                return true;
            case Menu.FIRST + 2:
                final Calendar calendar = Calendar.getInstance();
                final int year = calendar.get(Calendar.YEAR);
                final int month = calendar.get(Calendar.MONTH);
                final int day = calendar.get(Calendar.DAY_OF_MONTH);
                new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            mData.insertDate(new GregorianCalendar(year, month, day));
                            setData(mData);
                        }
                    }, year, month, day)
                    .show();
                return true;
            case Menu.FIRST + 3:
                final EditText editView = new EditText(this);
                editView.setSingleLine();
                new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("New Member")
                    .setView(editView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mData.insertEntry(editView.getText().toString().trim());
                            setData(mData);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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
            mData.deleteDate(col);
            setData(mData);
        } else if (v == mSide) {
            mData.deleteEntry(row);
            setData(mData);
        } else if (v == mSheet) {
            String[] symbolStrs = getResources().getStringArray(R.array.symbol_strings);
            ArrayList<String> attends = mData.entries.get(row).attends;
            if (symbolStrs.length == 0 || attends.size() - 1 < col) {
                return;
            }
            String attend = attends.get(col);
            if (attend == null) {
                attends.set(col, symbolStrs[0]);
            } else {
                boolean match = false;
                for (int i = 0; i < symbolStrs.length - 1; i++) {
                    if (symbolStrs[i].equals(attend)) {
                        attends.set(col, symbolStrs[i + 1]);
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    attends.set(col, null);
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

    private void setData(SheetData data) {
        mHeader.setData(data);
        mSide.setData(data);
        mSheet.setData(data);
    }
}
