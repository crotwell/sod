
package edu.sc.seis.sod.model.common;

import java.io.Serializable;
import java.time.Duration;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;


/**
 * SamplingImpl.java
 *
 *
 * Created: Wed Aug 11 11:02:00 1999
 *
 * @author Philip Crotwell
 * @version
 */

public class SamplingImpl implements Serializable {
    
    public int numPoints;

    public Duration interval;
    
    protected SamplingImpl() {}

    public static Serializable createEmpty() { return new SamplingImpl(); }

    public static SamplingImpl of(Channel chan) {
        return new SamplingImpl(1, Duration.ofNanos((long) (TimeUtils.NANOS_IN_SEC/chan.getSampleRate().getValue())));
    }
    
    public static SamplingImpl ofSamplesSeconds(int numPoints, double seconds) {
        return new SamplingImpl(numPoints, Duration.ofNanos(Math.round(seconds*TimeUtils.NANOS_IN_SEC)));
    }
    
    public SamplingImpl(int numPoints, Duration interval) {
        if (interval.getNano() == Long.MAX_VALUE && numPoints == 1) {
            // in this case, the DMC database likely had 0 sps, and converted
            // it to 1 sample in inifinity seconds. So we change to
            // 0 samples in 1 second, which is probably more correct
            interval = Duration.ofSeconds(1);
            numPoints = 0;
        }
        this.interval = interval;
        this.numPoints = numPoints;
    }


    /** Gets the sample period. Returns a Quantity object that has
     Units of time, usually seconds.
     @return the sample period in units of time
     */
    public Duration getPeriod() {
        return getTimeInterval().dividedBy(numPoints);
    }

    /** Gets the sample frequency. Returns a Quantity object that has
     units of 1/time, usually Hz.
     @return the sample frequency.
     */
    public QuantityImpl getFrequency() {
        return QuantityImpl.of(getTimeInterval()).inverse().multipliedByDbl(numPoints);
    }

    public int getNumPoints() { return numPoints; }
    
    protected void setNumPoints(int n) {
        this.numPoints = n;
    }

    public Duration getTimeInterval() { return interval; }

    protected void setTimeInterval(QuantityImpl i) {
        this.interval = Duration.ofNanos(Math.round(i.getValue(UnitImpl.NANOSECOND)));
    }
    
    public String toString() { return numPoints +" in "+interval; }

    public int hashCode(){
        int result = 38;
        result += 37* result + getTimeInterval().hashCode();
        result += 37 * result + getNumPoints();
        return result;
    }

    public boolean equals(Object o) {
        if (super.equals(o))  return true;
        if (o instanceof SamplingImpl) {
            SamplingImpl sampImpl = (SamplingImpl)o;
            if (sampImpl.getPeriod().equals(getPeriod()))  return true;
        }
        return false;
    }

} // SamplingImpl
