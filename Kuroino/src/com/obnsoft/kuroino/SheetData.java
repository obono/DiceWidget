package com.obnsoft.kuroino;

import java.util.ArrayList;
import java.util.Calendar;

public class SheetData {

    public int cellSize;
    public ArrayList<String> names = new ArrayList<String>();
    public ArrayList<Calendar> dates = new ArrayList<Calendar>();

    public void createNewData(Calendar begin, Calendar end,
            int often, boolean[] weekFlgs, String[] names) {
        this.names.clear();
        this.dates.clear();
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
            for (String name : names) {
                this.names.add(name);
            }
        }
    }

}
