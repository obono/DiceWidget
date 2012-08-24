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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StatsActivity extends ListActivity {

    private MyApplication   mApp;
    private DateFormat      mDateFormat;
    private DateFormat      mTimeFormat;

    /*-----------------------------------------------------------------------*/

    class MyAdapter extends ArrayAdapter<String> {

        class ViewHolder {
            TextView    tvCount;
            TextView    tvDate;
            ImageView[] ivDice = new ImageView[4];
        }

        private LayoutInflater  mInflater;
        private StringBuffer    mStrBuf = new StringBuffer();

        public MyAdapter(Context context, String[] objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.stats_item, null);
                holder = new ViewHolder();
                holder.tvCount = (TextView) convertView.findViewById(R.id.text_shake_count);
                holder.tvDate = (TextView) convertView.findViewById(R.id.text_shake_date);
                for (int i = 0; i < 4; i++) {
                    holder.ivDice[i] =
                        (ImageView) convertView.findViewById(MyApplication.IMAGE_IDS[i]);
                }
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvCount.setText(String.valueOf(position));
            long time = System.currentTimeMillis();
            mStrBuf.setLength(0);
            mStrBuf.append(mDateFormat.format(time)).append('\n').append(mTimeFormat.format(time));
            holder.tvDate.setText(mStrBuf.toString());
            for (int i = 0; i < 4; i++) {
                if (i < 4) {
                    holder.ivDice[i].setVisibility(View.VISIBLE);
                    holder.ivDice[i].setImageResource(R.drawable.dice);
                    holder.ivDice[i].getDrawable().setLevel((position + i) % 48);
                } else {
                    holder.ivDice[i].setVisibility(View.GONE);
                }
            }
            return convertView;
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
        mDateFormat = android.text.format.DateFormat.getDateFormat(this);
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(this);

        MyAdapter adapter = new MyAdapter(this, new String[100]); // TODO
        setListAdapter(adapter);
        for (int i = 0; i < 4; i++) {
            TextView tv = (TextView) findViewById(MyApplication.TEXT_IDS[i]);
            tv.setText(String.valueOf(i * 123)); // TODO
        }
    }

    public void onClickButton(View v) {
        finish();
        if (v == findViewById(R.id.button_config)) {
            mApp.setYellowMode(MyApplication.YELLOW_MODE_CONFIG);
            MyService.kickMyService(this, true);
            startActivity(new Intent(this, ConfigActivity.class));
        }
    }

}
