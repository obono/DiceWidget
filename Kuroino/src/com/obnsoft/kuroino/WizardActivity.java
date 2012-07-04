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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

public class WizardActivity extends Activity {

    private Calendar mCalFrom;
    private Calendar mCalTo;
    private DateFormat mDateFormat;
    private Button mBtnDateFrom;
    private Button mBtnDateTo;
    private EditText mEditTextMember;
    private int mIntervalDays = 1;
    private int mMemberCount = 0;
    private boolean[] mDweekFlgs = {false, true, true, true, true, true, false};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mCalFrom = new GregorianCalendar();
        int year = mCalFrom.get(Calendar.YEAR);
        int month = mCalFrom.get(Calendar.MONTH);
        mCalFrom.clear();
        mCalFrom.set(year, month, 1);
        mCalTo = new GregorianCalendar(
                year, month, mCalFrom.getActualMaximum(Calendar.DAY_OF_MONTH));
        mDateFormat = android.text.format.DateFormat.getDateFormat(this);

        mBtnDateFrom = (Button) findViewById(R.id.button_period_from);
        mBtnDateTo = (Button) findViewById(R.id.button_period_to);
        mBtnDateFrom.setText(mDateFormat.format(mCalFrom.getTime()));
        mBtnDateTo.setText(mDateFormat.format(mCalTo.getTime()));
        setOftenLabel();

        mEditTextMember = (EditText) findViewById(R.id.edittext_member);
        mEditTextMember.setFilters(new InputFilter[] {new MyInputFilter()});
        mEditTextMember.addTextChangedListener(new MyTextWatcher());
        setMemberCountLabel();
    }

    public void onPickDate(View view) {
        if (view == mBtnDateFrom) {
            pickDate(mBtnDateFrom, mCalFrom);
        } else if (view == mBtnDateTo) {
            pickDate(mBtnDateTo, mCalTo);
        }
    }

    public void onModifyOften(View view) {
        modifyOften();
    }

    public void onCreateSheet(View view) {
        SheetData data = ((MyApplication) getApplication()).getSheetData();
        data.createNewData(mCalFrom, mCalTo, mIntervalDays,
                (mIntervalDays == 0) ? mDweekFlgs : null,
                mEditTextMember.getText().toString().split("\n", SheetData.MAX_ROWS));
        setResult(RESULT_OK);
        finish();
    }

    private void pickDate(final Button btn, final Calendar cal) {
        DatePickerDialog.OnDateSetListener dl = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                cal.set(year, month, day);
                btn.setText(mDateFormat.format(cal.getTime()));
                setEnabledCreateButton();
            }
        };
        MyApplication.showDatePickerDialog(this, cal, dl);
    }

    private void modifyOften() {
        String[] items = getResources().getStringArray(R.array.often_configs);
        int choice = (mIntervalDays <= 1) ? 1 - mIntervalDays : 2;
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case 0: // Daily
                    mIntervalDays = 1;
                    setOftenLabel();
                    dialog.dismiss();
                    break;
                case 1: // Weekly
                    modifyOftenWeekly(dialog);
                    break;
                case 2: // Once every ~ days
                    modifyOftenEvery(dialog);
                    break;
                }
            }
        };
        MyApplication.showSingleChoiceDialog(
                this, android.R.drawable.ic_dialog_alert,
                R.string.msg_period_often, items, choice, listener);
    }

    private void modifyOftenWeekly(final DialogInterface parentDialog) {
        String[] items = getResources().getStringArray(R.array.dweek_configs);
        final boolean[] flgs = mDweekFlgs.clone();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                int count = 0;
                for (int i = 0; i < 7; i++) {
                    if (flgs[i]) count++;
                }
                if (count == 7) {
                    mIntervalDays = 1;
                } else {
                    mDweekFlgs = flgs;
                    mIntervalDays = 0;
                }
                setOftenLabel();
                parentDialog.dismiss();
            }
        };
        MyApplication.showMultiChoiceDialog(
                this, android.R.drawable.ic_dialog_alert,
                R.string.msg_period_days_of_week, items, flgs, listener);
    }

    private void modifyOftenEvery(final DialogInterface parentDialog) {
        final EditText edittext = new EditText(this);
        edittext.setSingleLine();
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        if (mIntervalDays >= 2) {
            edittext.setText(Integer.toString(mIntervalDays));
            edittext.selectAll();
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    int value = Integer.parseInt(edittext.getText().toString());
                    if (value > 0 && value < 100) {
                        mIntervalDays = value;
                    } else {
                        // TODO: notice error
                    }
                    setOftenLabel();
                    parentDialog.dismiss();
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        };
        MyApplication.showCustomDialog(
                this, android.R.drawable.ic_dialog_info,
                R.string.msg_period_interval_days, edittext, listener);
    }

    private void setOftenLabel() {
        String[] items = getResources().getStringArray(R.array.often_configs);
        TextView tv1 = (TextView) findViewById(R.id.text_often_main);
        TextView tv2 = (TextView) findViewById(R.id.text_often_sub);
        if (mIntervalDays == 1) {
            tv1.setText(items[0]);
            tv2.setVisibility(View.GONE);
        } else if (mIntervalDays == 0) {
            tv1.setText(items[1]);
            String[] dweeks = getResources().getStringArray(R.array.dweek_strings);
            StringBuffer buf = new StringBuffer();
            for (int i = 0; i < 7; i++) {
                if (mDweekFlgs[i]) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(dweeks[i]);
                }
            }
            tv2.setVisibility(View.VISIBLE);
            tv2.setText(buf.toString());
        } else {
            tv1.setText(String.format(
                    getText(R.string.text_period_often).toString(), mIntervalDays));
            tv2.setVisibility(View.GONE);
        }
    }

    private void setMemberCountLabel() {
        TextView textView = (TextView) findViewById(R.id.text_member_count);
        textView.setText(String.format(
                getText(R.string.text_member_count).toString(), mMemberCount));
    }

    private void setEnabledCreateButton() {
        Button button = (Button) findViewById(R.id.button_create_sheet);
        button.setEnabled(mMemberCount > 0 && mCalFrom.before(mCalTo));
    }

    /*----------------------------------------------------------------------*/

    class MyInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            boolean modified = false;
            StringBuffer buf = new StringBuffer();
            char lastChar = (dstart == 0) ? '\n' : dest.charAt(dstart - 1);
            for (int i = start; i < end; i++) {
                char c = source.charAt(i);
                if (lastChar == '\n' && isTruncateChar(c)) {
                    modified = true;
                } else {
                    buf.append(c);
                    lastChar = c;
                }
            }
            if (lastChar == '\n' && buf.length() > 0 &&
                    dend < dest.length() && dest.charAt(dend) == '\n') {
                buf.setLength(buf.length() - 1);
                mEditTextMember.setSelection(mEditTextMember.getSelectionStart() + 1);
                modified = true;
            }
            if (modified) {
                String s = buf.toString();
                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(s);
                    TextUtils.copySpansFrom((Spanned) source, start, end, null, sp, 0);
                    return sp;
                } else {
                    return s;
                }
            }
            return null; // keep original
        }
    }

    /*----------------------------------------------------------------------*/

    class MyTextWatcher implements TextWatcher {

        private static final int MSG_UPDATE_COUNT = 1;
        private Handler handler = new Handler() {
            @Override  
            public void dispatchMessage(Message msg) {
                if (msg.what == MSG_UPDATE_COUNT) {
                    mMemberCount = 0;
                    Editable names = mEditTextMember.getText();
                    boolean truncate = true;
                    for (int i = 0; i < names.length(); i++) {
                        char c = names.charAt(i);
                        if (truncate && !isTruncateChar(c)) {
                                truncate = false;
                                mMemberCount++;
                        } else if (!truncate && c == '\n') {
                            truncate = true;
                        }
                    }
                    setMemberCountLabel();
                    setEnabledCreateButton();
                }
            }
        };

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            ;
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ;
        }
        @Override
        public void afterTextChanged(Editable s) {
            handler.removeMessages(MSG_UPDATE_COUNT);
            handler.sendEmptyMessageDelayed(MSG_UPDATE_COUNT, 1000);
        }
    }

    private static boolean isTruncateChar(char c) {
        return (c == '\n' || c == ' ' || c == MyApplication.IDEOGRAPHICS_SPACE);
    }
}
