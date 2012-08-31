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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*  Control launching to avoid double widgets.  */
        Intent intent = getIntent();
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())) {
            int ret = RESULT_OK;
            if (AppWidgetManager.getInstance(this).getAppWidgetIds(
                    new ComponentName(this, MyWidgetProvider.class)).length >= 2) {
                Toast.makeText(this, R.string.msg_double, Toast.LENGTH_LONG).show();
                ret = RESULT_CANCELED;
            }
            setResult(ret, new Intent().putExtras(intent.getExtras()));
            finish();
            return;
        }

        /*  Show version information  */
        setContentView(R.layout.about);
        TextView textView = (TextView) findViewById(R.id.text_about_version);
        textView.setText(MyApplication.getVersion(this));
        try {
            StringBuilder buf = new StringBuilder();
            InputStream in = getResources().openRawResource(R.raw.license);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String str;
            while((str = reader.readLine()) != null) {
                buf.append(str).append('\n');
            }
            textView = (TextView) findViewById(R.id.text_about_message);
            textView.setText(buf.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onClickButton(View v) {
        MyApplication.hideNotice(this, MyApplication.NOTICE_ID_ABOUT);
        finish();
    }

}
