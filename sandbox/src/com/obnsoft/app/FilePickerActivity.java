package com.obnsoft.app;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class FilePickerActivity extends ListActivity {

    public static final String INTENT_EXTRA_PATH = "path";
    public static final String INTENT_EXTRA_EXTENSION = "extension";
    private String mPathCurrent;
    private String mExtension;
    private ArrayList<String> mPathStack = new ArrayList<String>();

    private FilePickerAdapter mAdapter;

    class FilePickerAdapter extends ArrayAdapter<File> {

        private Context mContext;

        class FilePickerViewHolder {
            public ImageView imageView;
            public TextView textView;
        }

        public FilePickerAdapter(Context context) {
            super(context, 0);
            mContext = context;
        }

        public void setTargetDirectory(String path) {
            clear();
            File dir = new File(path);
            if (dir != null) {
                File[] files = dir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.isHidden()) {
                            return false;
                        }
                        if (mExtension == null || file.isDirectory()) {
                            return true;
                        }
                        return file.getName().toLowerCase().endsWith(mExtension);
                    }
                });
                if (files != null) {
                    for (File file : files) {
                        add(file);
                    }
                    sort(new Comparator<File>() {
                        @Override
                        public int compare(File a, File b) {
                            if (a.isDirectory() != b.isDirectory()) {
                                return (a.isDirectory()) ? -1 : 1;
                            }
                            return a.getName().compareToIgnoreCase(b.getName());
                        }
                    });
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FilePickerViewHolder holder;
            if (convertView == null) {
                LinearLayout ll = new LinearLayout(mContext);
                ll.setGravity(Gravity.CENTER_VERTICAL);
                holder = new FilePickerViewHolder();
                holder.imageView = new ImageView(mContext);
                holder.textView = new TextView(mContext);
                ll.addView(holder.imageView);
                ll.addView(holder.textView);
                ll.setTag(holder);
                convertView = ll;
            } else {
                holder = (FilePickerViewHolder) convertView.getTag();
            }
            File file = (File) getItem(position);
            holder.imageView.setImageResource(file.isDirectory() ?
                    android.R.drawable.ic_menu_more : android.R.drawable.ic_menu_set_as);
            holder.textView.setSingleLine(true);
            holder.textView.setText(file.getName());
            holder.textView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            return convertView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String path = null;
        if (intent != null) {
            path = intent.getStringExtra(INTENT_EXTRA_PATH);
            mExtension = intent.getStringExtra(INTENT_EXTRA_EXTENSION);
            if (mExtension != null) {
                mExtension = mExtension.toLowerCase();
                if (!mExtension.startsWith(".")) {
                    mExtension = "." + mExtension;
                }
            }
        }
        if (path == null) {
            path = Environment.getExternalSDStorageDirectory().getPath();
        }

        mAdapter = new FilePickerAdapter(this);
        setListAdapter(mAdapter);
        setCurrentDirectory(path);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        File file = (File) mAdapter.getItem(position);
        if (file.isDirectory()) {
            mPathStack.add(mPathCurrent);
            setCurrentDirectory(file.getPath());
        } else {
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_PATH, file.getPath());
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        String path = getLastDirectory();
        if (path == null) {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        } else {
            mPathStack.remove(mPathStack.size() - 1);
            setCurrentDirectory(path);
        }
    }

    public void setCurrentDirectory(String path) {
        mPathCurrent = path;
        mAdapter.setTargetDirectory(path);
        onCurrentDirectoryChanged(path);
    }

    public void onCurrentDirectoryChanged(String path) {
        ;
    }

    public void goToUpperDirectory() {
        String path = getUpperDirectory();
        if (path != null) {
            mPathStack.add(mPathCurrent);
            setCurrentDirectory(path);
        }
    }

    public String getCurrentDirectory() {
        return mPathCurrent;
    }

    public String getLastDirectory() {
        int size = mPathStack.size();
        return (size > 0) ? mPathStack.get(size - 1) : null;
    }

    public String getUpperDirectory() {
        int start = mPathCurrent.length() - 1;
        if (mPathCurrent.endsWith(File.separator)) {
            start--;
        }
        int index = mPathCurrent.lastIndexOf(File.separatorChar, start);
        return (index >= 0) ? mPathCurrent.substring(0, index + 1) : null;
    }
}
