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
        int ampBorderWidth = sd.get(SeismogramDisplay.CENTER_LEFT).getWidth();
        flagLoc = ampBorderWidth + relFlagLocation + sd.getInsets().left;
        int timeBorderHeight = sd.get(SeismogramDisplay.TOP_CENTER).getHeight();
        flagY_bottom = sd.getHeight() - sd.getInsets().bottom;
        flagY_top = timeBorderHeight + sd.getInsets().top;
        return relFlagLocation;
    }

    public FlagData getFlagData() {
        FlagData flagData = new FlagData(flagLoc, flagY_top, flagY_bottom);
        return flagData;
    }

    private SeismogramDisplay sd = null;

    private int flagLoc = -1;

    private int flagY_bottom = -1;

    private int flagY_top = -1;
}