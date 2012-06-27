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

import java.io.File;
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
import android.widget.DatePicker;
import android.widget.EditText;

public class MainActivity extends Activity {

    private static final int MENU_GID_OPTION    = Menu.FIRST;
    private static final int MENU_GID_HEADER    = Menu.FIRST + 1;
    private static final int MENU_GID_SIDE      = Menu.FIRST + 2;

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
    private static final int MENU_ID_CREATE         = Menu.FIRST + 13;

    private static final int REQUEST_ID_IMPORT = 1;
    private static final int REQUEST_ID_EXPORT = 2;

    private int mTargetRow;
    private int mTargetCol;

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
        if ((new File(getLocalFileName())).exists()) {
            mData.importExternalData(getLocalFileName());
        } else {
            mData.createNewData(new GregorianCalendar(2012, 0, 1),
                    new GregorianCalendar(2012, 11, 31),
                    0,
                    new boolean[] {false, true, false, true, false, false, true},
                    new String[] {"Australia", "Brazil", "Canada", "Denmark",
                                    "Egypt", "France", "German"});
        }
        setData(mData);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (true) {
            mData.exportCurrentData(getLocalFileName());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.add(MENU_GID_OPTION, MENU_ID_CREATE, Menu.NONE, R.string.menu_new)
        .setIcon(android.R.drawable.ic_menu_edit);
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
            if (executeFunction(item.getItemId())) {
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v == mHeader) {
            Calendar cal = mData.dates.get(mTargetCol);
            menu.setHeaderTitle(DateFormat.getDateFormat(this).format(cal.getTime()));
            menu.add(MENU_GID_HEADER, MENU_ID_MODIFYDATE, Menu.NONE, R.string.menu_modify);
            menu.add(MENU_GID_HEADER, MENU_ID_DELETEDATE, Menu.NONE, R.string.menu_delete);
            menu.add(MENU_GID_HEADER, MENU_ID_INFODATE, Menu.NONE, R.string.menu_info);
            menu.add(MENU_GID_HEADER, MENU_ID_ADDDATE, Menu.NONE, R.string.menu_adddate);
        } else if (v == mSide) {
            menu.setHeaderTitle(mData.entries.get(mTargetRow).name);
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
        if (item.getGroupId() == MENU_GID_HEADER ||
                item.getGroupId() == MENU_GID_SIDE) {
            if (executeFunction(item.getItemId())) {
                return true;
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
        mTargetRow = row;
        mTargetCol = col;
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

    private boolean executeFunction(int menuId) {
        switch (menuId) {

        case MENU_ID_CREATE:
            // TODO: show confirmation!
            Intent intent0 = new Intent(this, WizardActivity.class);
            startActivity(intent0);
            return true;

        case MENU_ID_IMPORT:
            // TODO: show confirmation!
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
            DatePickerDialog.OnDateSetListener dl1 =
                    new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    Calendar cal1 = new GregorianCalendar(year, month, day);
                    if (mData.insertDate(cal1)) {
                        setData(mData);
                        handleFocus(null, -1, mData.searchDate(cal1, true));
                    }
                }
            };
            MyApplication.showDatePickerDialog(
                    this, Calendar.getInstance(), dl1);
            return true;

        case MENU_ID_MODIFYDATE:
            DatePickerDialog.OnDateSetListener dl2 =
                    new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    Calendar cal2 = new GregorianCalendar(year, month, day);
                    mData.moveDate(mTargetCol, cal2);
                    //setData(mData);
                    handleFocus(null, -1, mData.searchDate(cal2, true));
                }
            };
            MyApplication.showDatePickerDialog(
                    this, mData.dates.get(mTargetCol), dl2);
            return true;

        case MENU_ID_DELETEDATE:
            DialogInterface.OnClickListener cl0 = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mData.deleteDate(mTargetCol);
                    setData(mData);
                    handleFocus(null, -1, -1);
                }
            };
            MyApplication.showYesNoDialog(
                    this, android.R.drawable.ic_dialog_alert,
                    R.string.menu_delete, R.string.msg_delete, cl0);
            return true;

        case MENU_ID_INFODATE:
            // TODO
            return true;

        case MENU_ID_ADDMEMBER:
            mTargetRow = mData.entries.size();
        case MENU_ID_INSERTMEMBER:
            final EditText ev1 = new EditText(this);
            ev1.setSingleLine();
            DialogInterface.OnClickListener cl1 = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mData.insertEntry(mTargetRow, ev1.getText().toString());
                    handleFocus(null, mTargetRow, -1);
                    setData(mData);
                }
            };
            MyApplication.showCustomDialog(
                    this, android.R.drawable.ic_dialog_info,
                    R.string.msg_newmembername, ev1, cl1);
            return true;

        case MENU_ID_MODIFYMEMBER:
            final EditText ev2 = new EditText(this);
            ev2.setSingleLine();
            String name = mData.entries.get(mTargetRow).name;
            ev2.setText(name);
            ev2.selectAll();
            DialogInterface.OnClickListener cl2 = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mData.modifyEntry(mTargetRow, ev2.getText().toString());
                    setData(mData);
                }
            };
            MyApplication.showCustomDialog(
                    this, android.R.drawable.ic_dialog_info,
                    R.string.msg_newmembername, ev2, cl2);
            return true;

        case MENU_ID_MOVEUPMEMBER:
            if (mData.moveEntry(mTargetRow, -1)) {
                //setData(mData);
                handleFocus(null, mTargetRow - 1, -1);
            }
            return true;

        case MENU_ID_MOVEDOWNMEMBER:
            if (mData.moveEntry(mTargetRow, 1)) {
                //setData(mData);
                handleFocus(null, mTargetRow + 1, -1);
            }
            return true;

        case MENU_ID_DELETEMEMBER:
            DialogInterface.OnClickListener cl3 = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    mData.deleteEntry(mTargetRow);
                    setData(mData);
                    handleFocus(null, -1, -1);
                }
            };
            MyApplication.showYesNoDialog(
                    this, android.R.drawable.ic_dialog_alert,
                    R.string.menu_delete, R.string.msg_delete, cl3);
            return true;

        case MENU_ID_INFOMEMBER:
            // TODO
            return true;
        }
        return false;
    }

    private void setData(SheetData data) {
        mHeader.setData(data);
        mSide.setData(data);
        mSheet.setData(data);
    }

    private String getLocalFileName() {
        return getFilesDir() + File.pathSeparator + "work.csv";
    }
}
