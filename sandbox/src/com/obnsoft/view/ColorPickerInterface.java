package com.obnsoft.view;

public interface ColorPickerInterface {

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public void setColor(int color);
    public int getColor();
    public void setListener(OnColorChangedListener listener);
}
