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
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.DatePicker;
import android.widget.EditText;

public class MainActivity extends Activity {

    private static final int MENU_GID_OPTION    = 0;
    private static final int MENU_GID_HEADER    = 1;
    private static final int MENU_GID_SIDE      = 2;

    private static final int MENU_ID_IMPORT         = Menu.FIRST;
    private static final int MENU_ID_EXPORT         = Menu.FIRST + 1;
    private static final int MENU_ID_ADDDATE        = Menu.FIRST + 2;
    private static final int MENU_ID_MODIFYDATE     = Menu.FIRST + 3;
    private static final int MENU_ID_DELETEDATE     = Menu.FIRST + 4;
    private static final int MENU_ID_INFODATE       = Menu.FIRST + 5;
    private static final int MENU_ID_ADDMEMBER      = Menu.FIRST + 6;
    private static final int MENU_ID_MODIFYMEMBER   = Menu.FIRST + 7;
    private static final int MENU_ID_MOVEUPMEMBER   = Menu.FIRST + 8;
    private static final int MENU_ID_MOVEDOWNMEMBER = Menu.FIRST + 9;
    private static final int MENU_ID_DELETEMEMBER   = Menu.FIRST + 10;
    private static final int MENU_ID_INFOMEMBER     = Menu.FIRST + 11;
    private static final int MENU_ID_INSERTMEMBER   = Menu.FIRST + 12;

    private static final int REQUEST_ID_IMPORT = 1;
    private static final int REQUEST_ID_EXPORT = 2;

    private HeaderScrollView    mHeader;
    private SideScrollView      mSide;
    private SheetScrollView     mSheet;
    private SheetData           mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mHeader = (HeaderScrollView) findViewById(R.id.view_head);
        mSide = (SideScrollView) findViewById(R.id.view_side);
        mSheet = (SheetScrollView) findViewById(R.id.view_main);

        registerForContextMenu(mHeader);
        registerForContextMenu(mSide);

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
        menu.add(MENU_GID_OPTION, MENU_ID_IMPORT, Menu.NONE, R.string.menu_import)
            .setIcon(android.R.drawable.ic_menu_set_as);
        menu.add(MENU_GID_OPTION, MENU_ID_EXPORT, Menu.NONE, R.string.menu_export)
            .setIcon(android.R.drawable.ic_menu_save);
        menu.add(MENU_GID_OPTION, MENU_ID_ADDDATE, Menu.NONE, R.string.menu_adddate)
            .setIcon(android.R.drawable.ic_menu_day);
        menu.add(MENU_GID_OPTION, MENU_ID_ADDMEMBER, Menu.NONE, R.string.menu_addmember)
            .setIcon(android.R.drawable.ic_menu_add);
        return ret;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == MENU_GID_OPTION) {
            switch (item.getItemId()) {
            case MENU_ID_IMPORT:
                Intent intent1 = new Intent(this, MyFilePickerActivity.class);
                intent1.putExtra(MyFilePickerActivity.INTENT_EXTRA_TITLEID, R.string.title_import);
                intent1.putExtra(MyFilePickerActivity.INTENT_EXTRA_EXTENSION, "csv");
                startActivityForResult(intent1, REQUEST_ID_IMPORT);
                return true;
            case MENU_ID_EXPORT:
                Intent intent2 = new Intent(this, MyFilePickerActivity.class);
                intent2.putExtra(MyFilePickerActivity.INTENT_EXTRA_TITLEID, R.string.title_export);
                intent2.putExtra(MyFilePickerActivity.INTENT_EXTRA_EXTENSION, "csv");
                intent2.putExtra(MyFilePickerActivity.INTENT_EXTRA_WRITEMODE, true);
                startActivityForResult(intent2, REQUEST_ID_EXPORT);
                return true;
            case MENU_ID_ADDDATE:
                DatePickerDialog.OnDateSetListener listener1 =
                        new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        mData.insertDate(new GregorianCalendar(year, month, day));
                        setData(mData);
                    }
                };
                MyApplication.showDatePickerDialog(
                        this, Calendar.getInstance(), listener1);
                return true;
            case MENU_ID_ADDMEMBER:
                final EditText editView = new EditText(this);
                editView.setSingleLine();
                DialogInterface.OnClickListener listener2 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mData.insertEntry(editView.getText().toString().trim());
                        setData(mData);
                    }
                };
                MyApplication.showCustomDialog(
                        this, android.R.drawable.ic_dialog_info,
                        R.string.msg_newmembername, editView, listener2);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == mHeader) {
            int index = ((AdapterContextMenuInfo) menuInfo).position;
            Calendar cal = mData.dates.get(index);
            menu.setHeaderTitle(DateFormat.getDateFormat(this).format(cal.getTime()));
            menu.add(MENU_GID_HEADER, MENU_ID_MODIFYDATE, Menu.NONE, R.string.menu_modify);
            menu.add(MENU_GID_HEADER, MENU_ID_DELETEDATE, Menu.NONE, R.string.menu_delete);
            menu.add(MENU_GID_HEADER, MENU_ID_INFODATE, Menu.NONE, R.string.menu_info);
            menu.add(MENU_GID_HEADER, MENU_ID_ADDDATE, Menu.NONE, R.string.menu_adddate);
        } else if (v == mSide) {
            int index = ((AdapterContextMenuInfo) menuInfo).position;
            menu.setHeaderTitle(mData.entries.get(index).name);
            menu.add(MENU_GID_SIDE, MENU_ID_MODIFYMEMBER, Menu.NONE, R.string.menu_modify);
            menu.add(MENU_GID_SIDE, MENU_ID_MOVEUPMEMBER, Menu.NONE, R.string.menu_moveup);
            menu.add(MENU_GID_SIDE, MENU_ID_MOVEDOWNMEMBER, Menu.NONE, R.string.menu_movedown);
            menu.add(MENU_GID_SIDE, MENU_ID_DELETEMEMBER, Menu.NONE, R.string.menu_delete);
            menu.add(MENU_GID_SIDE, MENU_ID_INFOMEMBER, Menu.NONE, R.string.menu_info);
            menu.add(MENU_GID_SIDE, MENU_ID_INSERTMEMBER, Menu.NONE, R.string.menu_insertmember);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == MENU_GID_HEADER) {
            switch (item.getItemId()) {
                // TODO
                //mData.deleteDate(col);
                //setData(mData);
            }
        } else if (item.getGroupId() == MENU_GID_SIDE) {
            switch (item.getItemId()) {
                // TODO
                //mData.deleteEntry(row);
                //setData(mData);
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ID_IMPORT) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra(MyFilePickerActivity.INTENT_EXTRA_SELECTPATH);
                mData.importExternalData(path);
                setData(mData);
            }
        }
        if (requestCode == REQUEST_ID_EXPORT) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra(MyFilePickerActivity.INTENT_EXTRA_SELECTPATH);
                mData.exportCurrentData(path);
            }
        }
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
            openContextMenu(mHeader);
        } else if (v == mSide) {
            openContextMenu(mSide);
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
