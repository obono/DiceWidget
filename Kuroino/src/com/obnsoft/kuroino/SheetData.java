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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SheetData {

    public int cellSize;
    public ArrayList<Calendar> dates = new ArrayList<Calendar>();
    public ArrayList<EntryData> entries = new ArrayList<EntryData>();

    private static final String DATE_FORMAT = "yyyy/MM/dd";
    private static final String LF = "\r\n"; // System.getProperty("line.separator");

    /*----------------------------------------------------------------------*/

    class EntryData {

        public String name;
        public ArrayList<String> attends;

        public EntryData(String name, int size) {
            this.name = name;
            this.attends = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                this.attends.add(null);
            }
        }
    }

    /*----------------------------------------------------------------------*/

    public void createNewData(Calendar begin, Calendar end,
            int often, boolean[] weekFlgs, String[] names) {
        clear();
        if (begin == null || end == null) {
            return;
        }
        if (weekFlgs != null || often < 1) {
            often = 1;
        }
        Calendar cur = (Calendar) begin.clone();
        while (cur.before(end)) {
            if (weekFlgs == null || weekFlgs[cur.get(Calendar.DAY_OF_WEEK) - 1]) {
                this.dates.add((Calendar) cur.clone());
            }
            cur.add(Calendar.DAY_OF_MONTH, often);
        }

        if (names != null) {
            int size = this.dates.size();
            this.entries.ensureCapacity(names.length);
            for (String name : names) {
                this.entries.add(new EntryData(name, size));
            }
        }
    }

    public boolean importExternalData(String filePath) {
        boolean ret = false;
        clear();
        try {
            BufferedReader in  = new BufferedReader(new FileReader(new File(filePath)));
            String strBuf = in.readLine();
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            if (strBuf != null) {
                String[] ary = strBuf.split(",");
                boolean skipped = false;
                for (String strDate : ary) {
                    if (skipped) {
                        Calendar cal = new GregorianCalendar();
                        cal.setTime(df.parse(strDate));
                        this.dates.add(cal);
                    }
                    skipped = true;
                }
            }
            int size = this.dates.size();
            while ((strBuf = in.readLine()) != null) {
                String[] ary = strBuf.split(",");
                if (ary.length > 0) {
                    EntryData entry = new EntryData(ary[0], size);
                    for (int i = 1; i < ary.length && i <= size; i++) {
                        if (ary[i].length() > 0) {
                            entry.attends.set(i - 1, ary[i]);
                        }
                    }
                    this.entries.add(entry);
                }
            }
            in.close();
            ret = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean exportCurrentData(String filePath) {
        boolean ret = false;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(new File(filePath)));
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

    public void clear() {
        for (EntryData entry : this.entries) {
            entry.attends.clear();
        }
        this.entries.clear();
        this.dates.clear();
    }
}
