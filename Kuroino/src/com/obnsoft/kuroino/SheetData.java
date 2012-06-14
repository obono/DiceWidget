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

import java.util.ArrayList;
import java.util.Calendar;

public class SheetData {

    public int cellSize;
    public ArrayList<String> names = new ArrayList<String>();
    public ArrayList<Calendar> dates = new ArrayList<Calendar>();
    public ArrayList<String[]> attends = new ArrayList<String[]>();

    public void createNewData(Calendar begin, Calendar end,
            int often, boolean[] weekFlgs, String[] names) {
        this.names.clear();
        this.dates.clear();
        this.attends.clear();
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
            for (String name : names) {
                this.names.add(name);
                this.attends.add(new String[size]);
            }
        }
    }

}
