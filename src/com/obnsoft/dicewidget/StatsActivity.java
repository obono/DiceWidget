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

package com.obnsoft.dicewidget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StatsActivity extends ListActivity {

    private static final int[] E_IMAGE_IDS = {
        R.id.image_eye1, R.id.image_eye2, R.id.image_eye3,
        R.id.image_eye4, R.id.image_eye5, R.id.image_eye6,
    };

    private static final int[] E_TEXT_IDS = {
        R.id.text_number_e1, R.id.text_number_e2, R.id.text_number_e3,
        R.id.text_number_e4, R.id.text_number_e5, R.id.text_number_e6,
    };

    private MyApplication   mApp;
    private Cursor          mCursor;
    private DateFormat      mDateFormat;
    private DateFormat      mTimeFormat;

    /*-----------------------------------------------------------------------*/

    class MyAdapter extends CursorAdapter {

        class ViewHolder {
            TextView    tvCount;
            TextView    tvDate;
            ImageView[] ivDice = new ImageView[4];
        }

        private LayoutInflater  mInflater;
        private StringBuffer    mStrBuf = new StringBuffer();

        public MyAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.stats_item, null);
            ViewHolder holder = new ViewHolder();
            holder.tvCount = (TextView) view.findViewById(R.id.text_shake_count);
            holder.tvDate = (TextView) view.findViewById(R.id.text_shake_date);
            for (int i = 0; i < 4; i++) {
                holder.ivDice[i] = (ImageView) view.findViewById(MyApplication.IMAGE_IDS[i]);
            }
            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.tvCount.setText(String.valueOf(cursor.getInt(0)));
            int diceValue = cursor.getInt(1);
            for (int i = 0; i < 4; i++) {
                int level = (diceValue >> (i * 8)) & 0xFF;
                if (level != 0xFF) {
                    holder.ivDice[i].setVisibility(View.VISIBLE);
                    holder.ivDice[i].setImageResource(R.drawable.dice);
                    holder.ivDice[i].getDrawable().setLevel(level);
                } else {
                    holder.ivDice[i].setVisibility(View.GONE);
                }
            }
            long time = cursor.getLong(2);
            mStrBuf.setLength(0);
            mStrBuf.append(mDateFormat.format(time)).append('\n').append(mTimeFormat.format(time));
            holder.tvDate.setText(mStrBuf.toString());
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }

    /*-----------------------------------------------------------------------*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats);

        mApp = (MyApplication) getApplication();
        mCursor = mApp.getShakeRecordCursor();
        MyAdapter adapter = new MyAdapter(this, mCursor);
        setListAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDateFormat = android.text.format.DateFormat.getDateFormat(this);
        int resId = android.text.format.DateFormat.is24HourFormat(this) ?
                R.string.fmt_time24 : R.string.fmt_time12;
        mTimeFormat = new SimpleDateFormat(getString(resId));
        mCursor.requery();
        int[] countAry = mApp.getDiceCount();
        for (int i = 0; i < 6; i++) {
            ImageView iv = (ImageView) findViewById(E_IMAGE_IDS[i]);
            iv.getDrawable().setLevel(i * 2);
            TextView tv = (TextView) findViewById(E_TEXT_IDS[i]);
            tv.setText(String.valueOf(countAry[i]));
        }
        for (int i = 0; i < 4; i++) {
            TextView tv = (TextView) findViewById(MyApplication.TEXT_IDS[i]);
            tv.setText(String.valueOf(countAry[6 + i]));
        }
    }

    @Override
    protected void onDestroy() {
        mCursor.close();
        super.onDestroy();
    }

}
