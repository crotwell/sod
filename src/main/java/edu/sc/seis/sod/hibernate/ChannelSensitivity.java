package edu.sc.seis.sod.hibernate;

import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelImpl;


public class ChannelSensitivity {
    
    public ChannelSensitivity() {}

    public ChannelSensitivity(ChannelImpl channel, float overallGain, float frequency, UnitImpl inputUnits) {
        super();
        this.channel = channel;
        this.overallGain = overallGain;
        this.frequency = frequency;
        this.inputUnits = inputUnits;
    }
    
    public ChannelImpl getChannel() {
        return channel;
    }

    public float getOverallGain() {
        return overallGain;
    }
    
    public float getFrequency() {
        return frequency;
    }
    
    public UnitImpl getInputUnits() {
        return inputUnits;
    }
    
    protected void setChannel(ChannelImpl channel) {
        this.channel = channel;
    }

    
    protected void setOverallGain(float overallGain) {
        this.overallGain = overallGain;
    }

    
    protected void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    
    protected void setInputUnits(UnitImpl inputUnits) {
        this.inputUnits = inputUnits;
    }

    
    public int getDbid() {
        return dbid;
    }

    
    protected void setDbid(int dbid) {
        this.dbid = dbid;
    }
    
    public static boolean isNonChannelSensitivity(ChannelSensitivity sensitivity) {
        return sensitivity.getOverallGain() == 0 &&
        sensitivity.getFrequency() == 0 &&
        sensitivity.getInputUnits() == null;
    }
    
    public static ChannelSensitivity createNonChannelSensitivity(ChannelImpl chan) {
        return new ChannelSensitivity(chan, 0, 0, null);
    }

    ChannelImpl channel;
    float overallGain, frequency;
    UnitImpl inputUnits;
    int dbid;
}
