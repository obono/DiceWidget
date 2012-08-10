package com.obnsoft.dicewidget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

    private static final int NOTICE_ID_ABOUT = 1;

    private NotificationManager mNoticeMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoticeMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        /*  Version information  */
        String version = null;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            version = "Version " + packageInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        /*  Control launching to avoid double widgets.  */
        Intent intent = getIntent();
        if (AppWidgetManager.ACTION_APPWIDGET_CONFIGURE.equals(intent.getAction())) {
            int ret = RESULT_OK;
            if (AppWidgetManager.getInstance(this).getAppWidgetIds(
                    new ComponentName(this, MyWidgetProvider.class)).length >= 2) {
                Toast.makeText(this, R.string.msg_double, Toast.LENGTH_LONG).show();
                ret = RESULT_CANCELED;
            } else {
                Notification notice = new Notification(
                        R.drawable.icon, getText(R.string.app_name), System.currentTimeMillis());
                PendingIntent pIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, AboutActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK);
                notice.setLatestEventInfo(this, getText(R.string.app_name), version, pIntent);
                mNoticeMan.notify(NOTICE_ID_ABOUT, notice);
            }
            setResult(ret, new Intent().putExtras(intent.getExtras()));
            finish();
            return;
        }

        /*  Show version information  */
        setContentView(R.layout.about);
        TextView textView = (TextView) findViewById(R.id.text_about_version);
        textView.setText(version);
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
        mNoticeMan.cancel(NOTICE_ID_ABOUT);
        finish();
    }

}
