package edu.sc.seis.sod;

import java.io.Serializable;

public class FlagData implements Serializable {

    public FlagData(int flagLoc, int y_top, int y_bottom) {
        this.flagLoc = flagLoc;
        this.y_top = y_top;
        this.y_bottom = y_bottom;
    }

    public int getFlagLoc() {
        return this.flagLoc;
    }

    public int getFlagBottom() {
        return this.y_bottom;
    }

    public int getFlagTop() {
        return this.y_top;
    }

    public String toString() {
        return "Flag X: " + getFlagLoc() + " Bottom: " + getFlagBottom()
                + " Top: " + getFlagTop();
    }

    private int y_top = -1;

    private int y_bottom = -1;

    private int flagLoc = -1;
}