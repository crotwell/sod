package edu.sc.seis.sod;

import java.awt.Dimension;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.Flag;

public class SodFlag extends Flag {

    public SodFlag(MicroSecondDate flagTime, String name, SeismogramDisplay sd) {
        this(flagTime, name, sd, null);
    }

    public SodFlag(MicroSecondDate flagTime, String name, SeismogramDisplay sd,
            DrawableSeismogram seis) {
        super(flagTime, name, seis);
        this.sd = sd;
    }

    public int getFlagLocation(Dimension size, MicroSecondTimeRange timeRange) {
        int relFlagLocation = super.getFlagLocation(size, timeRange);
        x = relFlagLocation + sd.getInsets().left;
        if(sd.get(SeismogramDisplay.CENTER_LEFT) != null) {
            x += sd.get(SeismogramDisplay.CENTER_LEFT).getWidth();
        }
        top = sd.getInsets().top;
        if(sd.get(SeismogramDisplay.TOP_CENTER) != null) {
            top += sd.get(SeismogramDisplay.TOP_CENTER).getHeight();
        }
        bottom = sd.getHeight() - sd.getInsets().bottom + top;
        if(sd.get(SeismogramDisplay.BOTTOM_CENTER) != null) {
            bottom -= sd.get(SeismogramDisplay.BOTTOM_CENTER).getHeight();
        }
        return relFlagLocation;
    }

    public FlagData getFlagData() {
        FlagData flagData = new FlagData(x, top, bottom);
        return flagData;
    }

    private SeismogramDisplay sd = null;

    private int x = -1;

    private int bottom = -1;

    private int top = -1;
}