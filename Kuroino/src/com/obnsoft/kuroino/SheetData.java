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
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;

public class SheetData {

    public static final int MAX_ROWS = 256;
    public static final int MAX_COLS = 701;

    public static final int POS_KEEP = -1;
    public static final int POS_GONE = -2;

    public int cellSize;
    public ArrayList<Calendar> dates = new ArrayList<Calendar>();
    public ArrayList<EntryData> entries = new ArrayList<EntryData>();
    public String fileEncode = "UTF-8";

    private static final String DATE_FORMAT = "yyyy/MM/dd";
    private static final String LF = "\r\n"; // System.getProperty("line.separator");

    /*----------------------------------------------------------------------*/

    class EntryData {

        public String name;
        public ArrayList<String> attends;

        public EntryData(String name, int size) {
            setName(name);
            this.attends = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                this.attends.add(null);
            }
        }

        public void setName(String name) {
            if (name != null) {
                name = MyApplication.trimUni(name.replaceAll("[,\"]", ""));
            }
            this.name = name;
        }
    }

    /*----------------------------------------------------------------------*/

    public int getRowByCoord(float y) {
        int row = (int) y / cellSize;
        if (row < 0) row = 0;
        if (row >= entries.size()) row = entries.size() - 1;
        return row;
    }

    public int getColumnByCoord(float x) {
        int col = (int) x / cellSize;
        if (col < 0) col = 0;
        if (col >= dates.size()) col = dates.size() - 1;
        return col;
    }

    public void createNewData(Calendar begin, Calendar end,
            int often, boolean[] weekFlgs, String[] names) {
        clearAll();
        if (begin == null || end == null) {
            return;
        }
        if (weekFlgs != null || often < 1) {
            often = 1;
        }
        Calendar cur = (Calendar) begin.clone();
        int cols = 0;
        while (!cur.after(end)) {
            if (weekFlgs == null || weekFlgs[cur.get(Calendar.DAY_OF_WEEK) - 1]) {
                this.dates.add((Calendar) cur.clone());
            }
            cur.add(Calendar.DAY_OF_MONTH, often);
            if (++cols >= MAX_COLS) {
                break;
            }
        }

        int rows = 0;
        if (names != null) {
            this.entries.ensureCapacity(Math.min(names.length, MAX_ROWS));
            for (String name : names) {
                name = MyApplication.trimUni(name);
                if (name.length() > 0) {
                    this.entries.add(new EntryData(name, cols));
                    if (++rows >= MAX_ROWS) {
                        break;
                    }
                }
            }
        }
    }

    public boolean importExternalData(String filePath) {
        clearAll();
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filePath), fileEncode));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        }

        boolean ret = false;
        int cols = 0;
        String strBuf;
        try {
            strBuf = in.readLine();
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            if (strBuf != null) {
                String[] ary = strBuf.split(",");
                boolean skipped = false;
                for (String strDate : ary) {
                    if (skipped) {
                        Calendar cal = new GregorianCalendar();
                        cal.setTime(df.parse(strDate));
                        if (cols > 0 && !this.dates.get(cols - 1).before(cal)) {
                            break;
                        }
                        this.dates.add(cal);
                        if (++cols >= MAX_COLS) {
                            break;
                        }
                    }
                    skipped = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            // do nothing
        }

        int rows = 0;
        try {
            while ((strBuf = in.readLine()) != null) {
                String[] ary = strBuf.split(",");
                if (ary.length > 0) {
                    EntryData entry = new EntryData(ary[0], cols);
                    for (int i = 1; i < ary.length && i <= cols; i++) {
                        if (ary[i].length() > 0) {
                            entry.attends.set(i - 1, ary[i].substring(0, 1));
                        }
                    }
                    this.entries.add(entry);
                    if (++rows >= MAX_ROWS) {
                        break;
                    }
                }
            }
            in.close();
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public boolean exportCurrentData(String filePath) {
        boolean ret = false;
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath), fileEncode));
            StringBuffer buf = new StringBuffer();
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            for (Calendar cal : this.dates) {
                buf.append(',');
                buf.append(df.format(cal.getTime()));
            }
            out.write(buf.append(LF).toString());
            int size = this.dates.size();
            for (EntryData entry : this.entries) {
                buf.setLength(0);
                buf.append(entry.name);
                for (int i = 0; i < size; i++) {
                    buf.append(',');
                    String attend = entry.attends.get(i);
                    if (attend != null) {
                        buf.append(attend);
                    }
                }
                out.write(buf.append(LF).toString());
            }
            out.close();
            ret = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean insertDate(Calendar cal) {
        if (cal == null || this.dates.size() >= MAX_COLS) {
            return false;
        }
        int index = searchDate(cal, false);
        if (index < 0 || index >= this.dates.size()) {
            this.dates.add((Calendar) cal.clone());
            for (EntryData entry : this.entries) {
                entry.attends.add(null);
            }
        } else if (this.dates.get(index).equals(cal)){
            return false;
        } else {
            this.dates.add(index, (Calendar) cal.clone());
            for (EntryData entry : this.entries) {
                entry.attends.add(index, null);
            }
        }
        return true;
    }

    public boolean moveDate(int index, Calendar cal) {
        if (cal == null || index < 0 || index >= this.dates.size()) {
            return false;
        }
        int index2 = searchDate(cal, false);
        if (index2 < this.dates.size() && this.dates.get(index2).equals(cal)) {
            return false;
        }
        this.dates.set(index, (Calendar) cal.clone());
        if (index + 1 < index2) {
            index2--;
            Collections.rotate(this.dates.subList(index, index2 + 1), -1);
            for (EntryData entry : this.entries) {
                Collections.rotate(entry.attends.subList(index, index2 + 1), -1);
            }
        } else if (index > index2) {
            Collections.rotate(this.dates.subList(index2, index + 1), 1);
            for (EntryData entry : this.entries) {
                Collections.rotate(entry.attends.subList(index2, index + 1), 1);
            }
        }
        return true;
    }

    public int searchDate(Calendar cal, boolean match) {
        int lo = 0;
        int hi = this.dates.size() - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            Calendar cal2 = this.dates.get(mid);
            if (cal2.after(cal)) {
                hi = mid - 1;
            } else if (cal2.before(cal)) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }
        return (match) ? -1 : lo;
    }

    public boolean deleteDate(int index) {
        if (index < 0 || index >= this.dates.size()) {
            return false;
        }
        this.dates.remove(index);
        for (EntryData entry : this.entries) {
            entry.attends.remove(index);
        }
        return false;
    }

    public boolean insertEntry(String name) {
        return insertEntry(this.entries.size(), name);
    }

    public boolean insertEntry(int index, String name) {
        if (this.entries.size() >= MAX_ROWS) {
            return false;
        }
        EntryData entry = new EntryData(name, this.dates.size());
        if (index < 0 || index >= this.entries.size()) {
            this.entries.add(entry);
        } else {
            this.entries.add(index, entry);
        }
        return true;
    }

    public boolean moveEntry(int index, int distance) {
        if (index < 0 || index >= this.entries.size() ||
                index + distance < 0 || index + distance >= this.entries.size()) {
            return false;
        }
        if (distance > 0) {
            Collections.rotate(this.entries.subList(index, index + distance + 1), -1);
        } else if (distance < 0) {
            Collections.rotate(this.entries.subList(index + distance, index + 1), 1);
        }
        return true;
    }

    public boolean modifyEntry(int index, String name) {
        if (index < 0 || index >= this.entries.size()) {
            return false;
        }
        this.entries.get(index).setName(name);
        return true;
    }

    public boolean deleteEntry(int index) {
        if (index < 0 || index >= this.entries.size()) {
            return false;
        }
        this.entries.remove(index);
        return true;
    }

    public void clearAll() {
        for (EntryData entry : this.entries) {
            entry.attends.clear();
        }
        this.entries.clear();
        this.dates.clear();
    }
}
