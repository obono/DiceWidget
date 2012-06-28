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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.obnsoft.app.FilePickerActivity;

public class MyFilePickerActivity extends FilePickerActivity {

    public static final String INTENT_EXTRA_TITLEID = "titleId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.file_picker);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            int id = intent.getIntExtra(INTENT_EXTRA_TITLEID, 0);
            if (id != 0) {
                setTitle(id);
            }
        }
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

    @Override
    public void onFileSelected(final String path) {
        if (isWriteMode()) {
            DialogInterface.OnClickListener cl = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    setResultAndFinish(path);
                }
            };
            MyApplication.showYesNoDialog(
                    this, android.R.drawable.ic_dialog_alert,
                    R.string.menu_export, R.string.msg_overwrite, cl);
        } else {
            setResultAndFinish(path);
        }
    }

    @Override
    public void onNewFileRequested(final String directory, final String extension) {
        final EditText edittext = new EditText(this);
        edittext.setSingleLine();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String newPath = directory + edittext.getText().toString().trim();
                if (extension != null && !newPath.endsWith(extension)) {
                    newPath += extension;
                }
                setResultAndFinish(newPath);
            }
        };
        MyApplication.showCustomDialog(
                this, android.R.drawable.ic_dialog_info,
                R.string.msg_newfilename, edittext, listener);
    }

    public void onBackDirectory(View v) {
        onBackPressed();
    }

    public void onUpperDirectory(View v) {
        goToUpperDirectory();
    }
}
