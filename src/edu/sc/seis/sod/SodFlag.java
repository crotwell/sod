package edu.sc.seis.sod;
import java.awt.Dimension;
import java.awt.Graphics2D;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.BasicSeismogramDisplay;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.drawable.DrawableSeismogram;
import edu.sc.seis.fissuresUtil.display.drawable.Flag;
import edu.sc.seis.fissuresUtil.display.registrar.AmpEvent;
import edu.sc.seis.fissuresUtil.display.registrar.TimeEvent;

public class SodFlag extends Flag {
	public SodFlag(MicroSecondDate flagTime, String name,
				   BasicSeismogramDisplay bsd) {
		this(flagTime,name,bsd,null);
	}
	public SodFlag(MicroSecondDate flagTime, String name,
				   BasicSeismogramDisplay bsd, DrawableSeismogram seis) {
		super(flagTime,name,seis);
		this.bsd = bsd;
	}
	public void draw(Graphics2D canvas, Dimension size,
					 TimeEvent timeEvent, AmpEvent ampEvent) {
		super.draw(canvas,size,timeEvent,ampEvent);
	}
	public int getFlagLocation(Dimension size, MicroSecondTimeRange timeRange) {
		int relFlagLocation = super.getFlagLocation(size,timeRange);
		int ampBorderWidth = bsd.get(BasicSeismogramDisplay.CENTER_LEFT).getWidth();
		flagLoc = ampBorderWidth + relFlagLocation + bsd.getInsets().left;
		int timeBorderHeight = bsd.get(BasicSeismogramDisplay.TOP_CENTER).getHeight();
		flagY_bottom = bsd.getHeight()-bsd.getInsets().bottom;
		flagY_top = timeBorderHeight+bsd.getInsets().top;
		return relFlagLocation;
	}
	public FlagData getFlagData() {
		FlagData flagData = new FlagData(flagLoc,flagY_top,flagY_bottom);
		return flagData;
	}
	private BasicSeismogramDisplay bsd = null;
	private int flagLoc = -1;
	private int flagY_bottom = -1;
	private int flagY_top = -1;
}
