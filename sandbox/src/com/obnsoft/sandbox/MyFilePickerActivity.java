package com.obnsoft.sandbox;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.obnsoft.app.FilePickerActivity;

public class MyFilePickerActivity extends FilePickerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.file_picker);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCurrentDirectoryChanged(String path) {
        super.onCurrentDirectoryChanged(path);
        TextView tv = (TextView) findViewById(R.id.text_current_directory);
        tv.setText(getTrimmedCurrentDirectory(path));
        Button btn = (Button) findViewById(R.id.button_back_directory);
        btn.setEnabled(getLastDirectory() != null);
        btn = (Button) findViewById(R.id.button_upper_directory);
        btn.setEnabled(getUpperDirectory() != null);
    }

    public void onBackDirectory(View v) {
        onBackPressed();
    }

    public void onUpperDirectory(View v) {
        goToUpperDirectory();
    }
}
