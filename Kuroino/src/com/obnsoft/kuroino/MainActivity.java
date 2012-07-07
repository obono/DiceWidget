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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String PREFS_KEY_STAMPS    = "stamps";
    private static final String PREFS_KEY_CURSTAMP  = "current_stamp";
    private static final String PREFS_KEY_CURDIR    = "current_directory";

    private static final int MENU_GID_OPTION    = 1;
    private static final int MENU_GID_HEADER    = 2;
    private static final int MENU_GID_SIDE      = 3;

    private static final int MENU_ID_ADDDATE        = 1;
    private static final int MENU_ID_MODIFYDATE     = 2;
    private static final int MENU_ID_DELETEDATE     = 3;
    private static final int MENU_ID_INFODATE       = 4;
    private static final int MENU_ID_ADDMEMBER      = 5;
    private static final int MENU_ID_MODIFYMEMBER   = 6;
    private static final int MENU_ID_MOVEUPMEMBER   = 7;
    private static final int MENU_ID_MOVEDOWNMEMBER = 8;
    private static final int MENU_ID_DELETEMEMBER   = 9;
    private static final int MENU_ID_INFOMEMBER     = 10;
    private static final int MENU_ID_INSERTMEMBER   = 11;
    private static final int MENU_ID_CREATE         = 12;
    private static final int MENU_ID_IMPORT         = 13;
    private static final int MENU_ID_EXPORT         = 14;
    private static final int MENU_ID_ABOUT          = 15;

    private static final int REQUEST_ID_CREATE = 1;
    private static final int REQUEST_ID_IMPORT = 2;
    private static final int REQUEST_ID_EXPORT = 3;

    private static final int MSG_AUTOSAVE = 1;
    private static final int MSEC_TIMEOUT_AUTOSAVE = 60 * 1000;

    private int mTargetRow = SheetData.POS_GONE;
    private int mTargetCol = SheetData.POS_GONE;
    private String mStamps;
    private String mCurStamp;
    private String mCurDir;
    private boolean mIsDirtySheetData = false;
    private boolean mIsDirtyPreferences = false;
    private Handler mTimeoutHandler = new Handler() {
        @Override  
        public void dispatchMessage(Message msg) {
            if (msg.what == MSG_AUTOSAVE) {
                autoSave();
            }
        }
    };

    private HeaderScrollView    mHeader;
    private SideScrollView      mSide;
    private SheetScrollView     mSheet;
    private SheetData           mData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mStamps = prefs.getString(PREFS_KEY_STAMPS,
                getText(R.string.default_stamps).toString());
        mCurStamp = prefs.getString(PREFS_KEY_CURSTAMP, mStamps);
        mCurDir = prefs.getString(PREFS_KEY_CURDIR, MyFilePickerActivity.DEFAULT_DIRECTORY);

        mHeader = (HeaderScrollView) findViewById(R.id.view_head);
        mSide = (SideScrollView) findViewById(R.id.view_side);
        mSheet = (SheetScrollView) findViewById(R.id.view_main);

        registerForContextMenu(mHeader);
        registerForContextMenu(mSide);

        mData = ((MyApplication) getApplication()).getSheetData();
        mHeader.setSheetData(mData);
        mSide.setSheetData(mData);
        mSheet.setSheetData(mData);

        Calendar calNow = new GregorianCalendar();
        int year = calNow.get(Calendar.YEAR);
        int month = calNow.get(Calendar.MONTH);
        int day = calNow.get(Calendar.DAY_OF_MONTH);
        calNow.clear();
        calNow.set(year, month, day);
        int col = mData.searchDate(calNow, false);
        if (col >= mData.dates.size()) {
            col = mData.dates.size() - 1;
        }
        handleFocus(null, SheetData.POS_KEEP, col, true);
        setStampLabel(mCurStamp);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshViews();
    }

    @Override
    protected void onPause() {
        mTimeoutHandler.removeMessages(MSG_AUTOSAVE);
        autoSave();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.add(MENU_GID_OPTION, MENU_ID_ADDDATE, Menu.NONE, R.string.menu_adddate)
            .setIcon(android.R.drawable.ic_menu_today);
        menu.add(MENU_GID_OPTION, MENU_ID_ADDMEMBER, Menu.NONE, R.string.menu_addmember)
            .setIcon(android.R.drawable.ic_menu_my_calendar);
        menu.add(MENU_GID_OPTION, MENU_ID_CREATE, Menu.NONE, R.string.menu_new)
            .setIcon(R.drawable.ic_menu_newfile);
        menu.add(MENU_GID_OPTION, MENU_ID_IMPORT, Menu.NONE, R.string.menu_import)
            .setIcon(R.drawable.ic_menu_import);
        menu.add(MENU_GID_OPTION, MENU_ID_EXPORT, Menu.NONE, R.string.menu_export)
            .setIcon(R.drawable.ic_menu_export);
        menu.add(MENU_GID_OPTION, MENU_ID_ABOUT, Menu.NONE, R.string.menu_version)
            .setIcon(android.R.drawable.ic_menu_info_details);
        return ret;
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
            menu.setHeaderIcon(R.drawable.ic_dialog_date);
            menu.setHeaderTitle(MyApplication.getDateString(this, mData.dates.get(mTargetCol)));
            menu.add(MENU_GID_HEADER, MENU_ID_MODIFYDATE, Menu.NONE, R.string.menu_modify);
            menu.add(MENU_GID_HEADER, MENU_ID_DELETEDATE, Menu.NONE, R.string.menu_delete);
            menu.add(MENU_GID_HEADER, MENU_ID_INFODATE, Menu.NONE, R.string.menu_info);
            menu.add(MENU_GID_HEADER, MENU_ID_ADDDATE, Menu.NONE, R.string.menu_adddate);
        } else if (v == mSide) {
            menu.setHeaderIcon(R.drawable.ic_dialog_member);
            menu.setHeaderTitle(mData.entries.get(mTargetRow).name);
            menu.add(MENU_GID_SIDE, MENU_ID_MODIFYMEMBER, Menu.NONE, R.string.menu_modify);
            if (mTargetRow > 0) {
                menu.add(MENU_GID_SIDE, MENU_ID_MOVEUPMEMBER, Menu.NONE, R.string.menu_moveup);
            }
            if (mTargetRow < mData.entries.size() - 1) {
                menu.add(MENU_GID_SIDE, MENU_ID_MOVEDOWNMEMBER, Menu.NONE, R.string.menu_movedown);
            }
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
        switch (requestCode) {
        case REQUEST_ID_CREATE:
            if (resultCode == RESULT_OK) {
                setSheetDataIsDirty();
                handleFocus(null, SheetData.POS_GONE, SheetData.POS_GONE, false);
                mSheet.scrollTo(0, 0);
            }
            break;
        case REQUEST_ID_IMPORT:
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra(MyFilePickerActivity.INTENT_EXTRA_SELECTPATH);
                mData.importDataFromFile(path);
                mCurDir = path.substring(0, path.lastIndexOf(File.separatorChar) + 1);
                setSheetDataIsDirty();
                setPreferencesIsDirty();
                refreshViews();
                handleFocus(null, SheetData.POS_GONE, SheetData.POS_GONE, false);
                mSheet.scrollTo(0, 0);
            }
            break;
        case REQUEST_ID_EXPORT:
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra(MyFilePickerActivity.INTENT_EXTRA_SELECTPATH);
                mData.exportDataToFile(path);
                mCurDir = path.substring(0, path.lastIndexOf(File.separatorChar) + 1);
                setPreferencesIsDirty();
            }
            break;
        }
    }

    /*----------------------------------------------------------------------*/

    protected void handleFocus(View v, int row, int col, boolean scroll) {
        if (v != mHeader) {
            mHeader.setFocus(col, scroll);
        }
        if (v != mSide) {
            mSide.setFocus(row, scroll);
        }
        if (v != mSheet) {
            mSheet.setFocus(row, col);
        }
    }

    protected void handleMouseDown(View v) {
        if (v != mHeader) {
            mHeader.fling(0);
        }
        if (v != mSide) {
            mSide.fling(0);
        }
        if (v != mSheet) {
            mSheet.fling(0, 0);
        }
    }

    protected void handleClick(View v, int row, int col, boolean extra) {
        if (v == mHeader) {
            if (extra) {
                mTargetCol = col;
                openContextMenu(mHeader);
            } else {
                handleFocus(v, SheetData.POS_KEEP, col, false);
            }
        } else if (v == mSide) {
            if (extra) {
                mTargetRow = row;
                openContextMenu(mSide);
            } else {
                handleFocus(v, row, SheetData.POS_KEEP, false);
            }
        } else if (v == mSheet) {
            handleFocus(v, row, col, false);
            changeStamp(row, col, mCurStamp);
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
        case MENU_ID_ADDDATE:
            addDate();
            return true;
        case MENU_ID_MODIFYDATE:
            modifyDate(mTargetCol);
            return true;
        case MENU_ID_DELETEDATE:
            deleteDate(mTargetCol);
            return true;
        case MENU_ID_INFODATE:
            showDateStats(mTargetCol);
            return true;
        case MENU_ID_ADDMEMBER:
            addMember(mData.entries.size());
            return true;
        case MENU_ID_MODIFYMEMBER:
            modifyMember(mTargetRow);
            return true;
        case MENU_ID_MOVEUPMEMBER:
            moveMember(mTargetRow, -1);
            return true;
        case MENU_ID_MOVEDOWNMEMBER:
            moveMember(mTargetRow, 1);
            return true;
        case MENU_ID_DELETEMEMBER:
            deleteMember(mTargetRow);
            return true;
        case MENU_ID_INFOMEMBER:
            showMemberStats(mTargetRow);
            return true;
        case MENU_ID_INSERTMEMBER:
            addMember(mTargetRow);
            return true;
        case MENU_ID_CREATE:
            startWizardActivity();
            return true;
        case MENU_ID_IMPORT:
            startFilePickerActivityToImport();
            return true;
        case MENU_ID_EXPORT:
            startFilePickerActivityToExport();
            return true;
        case MENU_ID_ABOUT:
            showVersion();
            return true;
        }
        return false;
    }

    public void onSelectStamp(View v) {
        selectStamp();
    }

    /*----------------------------------------------------------------------*/

    private void addDate() {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar cal = new GregorianCalendar(year, month, day);
                if (mData.insertDate(cal)) {
                    setSheetDataIsDirty();
                    refreshViews();
                    handleFocus(null, SheetData.POS_KEEP, mData.searchDate(cal, true), true);
                }
            }
        };
        MyApplication.showDatePickerDialog(this, new GregorianCalendar(), listener);
    }

    private void modifyDate(final int col) {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                Calendar cal = new GregorianCalendar(year, month, day);
                mData.moveDate(col, cal);
                setSheetDataIsDirty();
                handleFocus(null, SheetData.POS_KEEP, mData.searchDate(cal, true), true);
            }
        };
        MyApplication.showDatePickerDialog(this, mData.dates.get(col), listener);
    }

    private void deleteDate(final int col) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mData.deleteDate(col);
                setSheetDataIsDirty();
                refreshViews();
                handleFocus(null, SheetData.POS_KEEP, SheetData.POS_GONE, false);
            }
        };
        MyApplication.showYesNoDialog(
                this, android.R.drawable.ic_dialog_alert,
                MyApplication.getDateString(this, mData.dates.get(col)),
                R.string.msg_delete, listener);
    }

    private void showDateStats(int col) {
        class StampStats {
            int count = 0;
            StringBuffer buf = new StringBuffer();
        }
        LinkedHashMap<String, StampStats> map = new LinkedHashMap<String, StampStats>();
        for (int i = 0; i < mStamps.length(); i++) {
            map.put(mStamps.substring(i, i + 1), new StampStats());
        }
        for (SheetData.EntryData entry : mData.entries) {
            String key = entry.attends.get(col);
            if (key != null) {
                StampStats stats = map.get(key);
                if (stats == null) {
                    stats = new StampStats();
                    map.put(key, stats);
                }
                stats.count++;
                stats.buf.append("\n - ").append(entry.name);
            }
        }
        StringBuffer msgBuf = new StringBuffer();
        for (String key : map.keySet()) {
            if (msgBuf.length() > 0) {
                msgBuf.append("\n\n");
            }
            StampStats stats = map.get(key);
            msgBuf.append(key).append(": ")
                .append(String.format(getText(R.string.text_member_count).toString(), stats.count))
                .append(stats.buf);
        }
        MyApplication.showShareDialog(this, R.drawable.ic_dialog_date,
                MyApplication.getDateString(this, mData.dates.get(col)), msgBuf);
    }

    /*----------------------------------------------------------------------*/

    private void addMember(final int row) {
        final EditText editText = new EditText(this);
        editText.setSingleLine();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mData.insertEntry(row, editText.getText().toString());
                setSheetDataIsDirty();
                refreshViews();
                handleFocus(null, row, SheetData.POS_KEEP, true);
            }
        };
        MyApplication.showCustomDialog(
                this, R.drawable.ic_dialog_member,
                R.string.msg_newmembername, editText, listener);
    }

    private void modifyMember(final int row) {
        final EditText editText = new EditText(this);
        editText.setSingleLine();
        String name = mData.entries.get(row).name;
        editText.setText(name);
        editText.setSelection(name.length());
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mData.modifyEntry(row, editText.getText().toString());
                setSheetDataIsDirty();
                refreshViews();
            }
        };
        MyApplication.showCustomDialog(
                this, R.drawable.ic_dialog_member,
                R.string.msg_newmembername, editText, listener);
    }

    private void moveMember(int row, int distance) {
        if (mData.moveEntry(row, distance)) {
            setSheetDataIsDirty();
            handleFocus(null, row + distance, SheetData.POS_KEEP, true);
        }
    }

    private void deleteMember(final int row) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                mData.deleteEntry(row);
                setSheetDataIsDirty();
                refreshViews();
                handleFocus(null, SheetData.POS_GONE, SheetData.POS_KEEP, false);
            }
        };
        MyApplication.showYesNoDialog(
                this, android.R.drawable.ic_dialog_alert,
                mData.entries.get(row).name, R.string.msg_delete, listener);
    }

    private void showMemberStats(int row) {
        class StampStats {
            int count = 0;
        }
        LinkedHashMap<String, StampStats> map = new LinkedHashMap<String, StampStats>();
        for (int i = 0; i < mStamps.length(); i++) {
            map.put(mStamps.substring(i, i + 1), new StampStats());
        }
        for (String key : mData.entries.get(row).attends) {
            if (key != null) {
                StampStats stats = map.get(key);
                if (stats == null) {
                    stats = new StampStats();
                    map.put(key, stats);
                }
                stats.count++;
            }
        }
        StringBuffer msgBuf = new StringBuffer();
        msgBuf.append(MyApplication.getDateString(this, mData.dates.get(0))).append(" - ")
            .append(MyApplication.getDateString(this, mData.dates.get(mData.dates.size() - 1)));
        for (String key : map.keySet()) {
            msgBuf.append('\n').append(key).append(": ").append(String.format(
                    getText(R.string.text_times_count).toString(), map.get(key).count));
        }
        MyApplication.showShareDialog(this, R.drawable.ic_dialog_member,
                mData.entries.get(row).name, msgBuf);
    }

    private void showVersion() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View aboutView = inflater.inflate(R.layout.about, null);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            TextView textView = (TextView) aboutView.findViewById(R.id.text_about_version);
            textView.setText("Version " + packageInfo.versionName);

            StringBuilder buf = new StringBuilder();
            InputStream in = getResources().openRawResource(R.raw.license);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String str;
            while((str = reader.readLine()) != null) {
                buf.append(str).append('\n');
            }
            textView = (TextView) aboutView.findViewById(R.id.text_about_message);
            textView.setText(buf.toString());
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MyApplication.showCustomDialog(this, android.R.drawable.ic_dialog_info,
                R.string.menu_version, aboutView, null);
    }

    /*----------------------------------------------------------------------*/

    private void startWizardActivity() {
        final Intent intent = new Intent(this, WizardActivity.class);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                startActivityForResult(intent, REQUEST_ID_CREATE);
            }
        };
        MyApplication.showYesNoDialog(
                this, android.R.drawable.ic_dialog_alert,
                R.string.menu_new, R.string.msg_newsheet, listener);
    }

    private void startFilePickerActivityToImport() {
        final Intent intent = new Intent(this, MyFilePickerActivity.class);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_TITLEID, R.string.title_import);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_DIRECTORY, mCurDir);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_EXTENSION, "csv");
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                startActivityForResult(intent, REQUEST_ID_IMPORT);
            }
        };
        MyApplication.showYesNoDialog(
                this, android.R.drawable.ic_dialog_alert,
                R.string.menu_import, R.string.msg_newsheet, listener);
    }

    private void startFilePickerActivityToExport() {
        Intent intent = new Intent(this, MyFilePickerActivity.class);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_TITLEID, R.string.title_export);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_DIRECTORY, mCurDir);
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_EXTENSION, "csv");
        intent.putExtra(MyFilePickerActivity.INTENT_EXTRA_WRITEMODE, true);
        startActivityForResult(intent, REQUEST_ID_EXPORT);
    }

    /*----------------------------------------------------------------------*/

    private void changeStamp(int row, int col, String stamp) {
        ArrayList<String> attends = mData.entries.get(row).attends;
        if (attends.size() - 1 < col) {
            return;
        }
        String attend = attends.get(col);
        if (stamp == null || stamp.length() == 0) {
            attends.set(col, null);
        } else if (stamp.length() == 1) {
            if (attend == null) {
                attends.set(col, stamp);
            } else if (stamp.equals(attend)) {
                attends.set(col, null);
            }
        } else {
            if (attend == null) {
                attends.set(col, stamp.substring(0, 1));
            } else {
                int index = stamp.indexOf(attend);
                if (index >= 0 && index < stamp.length() - 1) {
                    attends.set(col, stamp.substring(index + 1, index + 2));
                } else {
                    attends.set(col, null);
                }
            }
        }
        setSheetDataIsDirty();
    }

    private void selectStamp() {
        final int count = mStamps.length();
        final boolean notSingle = (count >= 2);

        String[] items = new String[count + (notSingle ? 4 : 3)];
        int index = 0;
        if (notSingle) {
            items[index++] = getText(R.string.text_stamp_toggle).toString();
        }
        String format = getText(R.string.text_stamp_format).toString();
        for (int i = 0; i < count; i++) {
            items[index++] = String.format(format, mStamps.charAt(i));
        }
        items[index++] = getText(R.string.text_stamp_other).toString();
        items[index++] = getText(R.string.text_stamp_erase).toString();
        items[index++] = getText(R.string.text_stamp_config).toString();

        int choice = -1;
        if (mCurStamp == null || mCurStamp.length() == 0) {
            choice = count + 1;
        } else if (mCurStamp.length() == 1) {
            choice = mStamps.indexOf(mCurStamp);
            if (choice == -1) {
                choice = count;
            }
        }
        if (notSingle) {
            choice++;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean close = true;
                if (notSingle && --which == -1) { // Toggle
                    mCurStamp = mStamps;
                } else if (which < count) {
                    mCurStamp = mStamps.substring(which, which + 1);
                } else {
                    which -= count;
                    if (which == 0) { // Other
                        selectStampOther(dialog);
                        close = false;
                    } else if (which == 1) { // Erase
                        mCurStamp = "";
                    } else if (which == 2) { // Configuration
                        selectStampConfig(dialog);
                        close = false;
                    }
                }
                if (close) {
                    setPreferencesIsDirty();
                    setStampLabel(mCurStamp);
                    dialog.dismiss();
                }
            }
        };
        MyApplication.showSingleChoiceDialog(
                this, R.drawable.ic_dialog_stamp,
                R.string.msg_stamp_select, items, choice, listener);
    }

    private void selectStampOther(final DialogInterface parentDialog) {
        final EditText editText = new EditText(this);
        editText.setSingleLine();
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        if (mCurStamp != null && mCurStamp.length() == 1) {
            editText.setText(mCurStamp);
            editText.selectAll();
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String str = editText.getText().toString();
                if (str.length() == 1) {
                    mCurStamp = str;
                    setPreferencesIsDirty();
                    setStampLabel(str);
                    parentDialog.dismiss();
                } else {
                    // TODO
                }
            }
        };
        MyApplication.showCustomDialog(
                this, R.drawable.ic_dialog_stamp, R.string.msg_stamp_other, editText, listener);
    }

    private void selectStampConfig(final DialogInterface parentDialog) {
        final EditText editText = new EditText(this);
        editText.setSingleLine();
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        editText.setText(mStamps);
        editText.setSelection(mStamps.length());
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String str = editText.getText().toString();
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < str.length(); i++) {
                    String substr = str.substring(i, i + 1);
                    if (buf.indexOf(substr) == -1) {
                        buf.append(substr);
                    }
                }
                if (buf.length() > 0) {
                    mStamps = buf.toString().replaceAll("[,\" \u3000]", "");
                    mCurStamp = mStamps;
                    setPreferencesIsDirty();
                    setStampLabel(mCurStamp);
                    parentDialog.dismiss();
                    selectStamp();
                } else {
                    // TODO
                }
            }
        };
        MyApplication.showCustomDialog(
                this, R.drawable.ic_dialog_stamp, R.string.msg_stamp_config, editText, listener);
    }

    private void setStampLabel(String stamp) {
        TextView textView = (TextView) findViewById(R.id.text_stamp);
        if (stamp == null || stamp.length() == 0) {
            textView.setTextAppearance(this, android.R.style.TextAppearance_Small);
            textView.setText(R.string.text_stamp_erase);
        } else if (stamp.length() == 1) {
            textView.setTextAppearance(this, android.R.style.TextAppearance_Large);
            textView.setText(stamp);
        } else {
            textView.setTextAppearance(this, android.R.style.TextAppearance_Small);
            textView.setText(R.string.text_stamp_toggle);
        }
    }

    /*----------------------------------------------------------------------*/

    private void setSheetDataIsDirty() {
        mIsDirtySheetData = true;
        mTimeoutHandler.removeMessages(MSG_AUTOSAVE);
        mTimeoutHandler.sendEmptyMessageDelayed(MSG_AUTOSAVE, MSEC_TIMEOUT_AUTOSAVE);
    }

    private void setPreferencesIsDirty() {
        mIsDirtyPreferences = true;
        mTimeoutHandler.removeMessages(MSG_AUTOSAVE);
        mTimeoutHandler.sendEmptyMessageDelayed(MSG_AUTOSAVE, MSEC_TIMEOUT_AUTOSAVE);
    }

    private void autoSave() {
        if (mIsDirtySheetData) {
            mIsDirtySheetData = false;
            ((MyApplication) getApplication()).saveSheetData();
        }
        if (mIsDirtyPreferences) {
            mIsDirtyPreferences = false;
            SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(PREFS_KEY_STAMPS, mStamps);
            editor.putString(PREFS_KEY_CURSTAMP, mCurStamp);
            editor.putString(PREFS_KEY_CURDIR, mCurDir);
            editor.commit();
        }
    }

    private void refreshViews() {
        mHeader.refreshView();
        mSide.refreshView();
        mSheet.refreshView();
    }

}
