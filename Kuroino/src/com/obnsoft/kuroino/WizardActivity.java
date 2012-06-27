package com.obnsoft.kuroino;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
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
    private int mIntervalDays = 1;
    private boolean[] mDweekFlgs = new boolean[7];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mCalFrom = Calendar.getInstance();
        mCalTo = Calendar.getInstance();
        mCalTo.add(Calendar.MONTH, 1);
        mDateFormat = android.text.format.DateFormat.getDateFormat(this);

        mBtnDateFrom = (Button) findViewById(R.id.button_period_from);
        mBtnDateTo = (Button) findViewById(R.id.button_period_to);
        mBtnDateFrom.setText(mDateFormat.format(mCalFrom.getTime()));
        mBtnDateTo.setText(mDateFormat.format(mCalTo.getTime()));

        final EditText memberView = (EditText) findViewById(R.id.edittext_member);
        final TextView memberCountView = (TextView) findViewById(R.id.text_member_count);
        memberView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    StringBuffer buf = new StringBuffer();
                    String[] members =
                        memberView.getText().toString().split("\n", SheetData.MAX_ROWS);
                    int count = 0;
                    for (String member : members) {
                        member = member.trim();
                        if (member.length() > 0) {
                            if (count > 0) {
                                buf.append('\n');
                            }
                            buf.append(member);
                            count++;
                        }
                    }
                    memberView.setText(buf.toString());
                    memberCountView.setText(Integer.toString(count));
                }
            }
        });

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

    public void onCreate(View view) {
        finish();
    }

    private void pickDate(final Button btn, final Calendar cal) {
        DatePickerDialog.OnDateSetListener dl = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                cal.set(year, month, day);
                btn.setText(mDateFormat.format(cal.getTime()));
            }
        };
        MyApplication.showDatePickerDialog(this, cal, dl);
    }

    private void modifyOften() {
        String[] items = getResources().getStringArray(R.array.often_configs);
        int choice = (mIntervalDays <= 1) ? 1 - mIntervalDays : 2;
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case 0: // Daily
                    mIntervalDays = 1;
                    dialog.dismiss();
                    // TODO
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
                R.string.menu_delete, items, choice, listener);
    }

    private void modifyOftenWeekly(final DialogInterface parentDialog) {
        String[] items = getResources().getStringArray(R.array.dweek_strings);
        final boolean[] flgs = mDweekFlgs.clone();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mDweekFlgs = flgs;
                mIntervalDays = 0;
                parentDialog.dismiss();
                // TODO
            }
        };
        MyApplication.showMultiChoiceDialog(
                this, android.R.drawable.ic_dialog_alert,
                R.string.menu_delete, items, flgs, listener);
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
            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    int value = Integer.parseInt(edittext.getText().toString());
                    if (value > 1 && value < 100) {
                        mIntervalDays = value;
                    } else {
                        // TODO
                    }
                    parentDialog.dismiss();
                    // TODO
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        };
        MyApplication.showCustomDialog(
                this, android.R.drawable.ic_dialog_info,
                R.string.msg_newmembername, edittext, listener);
    }
}
