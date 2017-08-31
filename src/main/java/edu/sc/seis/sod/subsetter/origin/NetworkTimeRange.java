package edu.sc.seis.sod.subsetter.origin;

import java.util.List;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.Arm;
import edu.sc.seis.sod.ArmListener;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.EventAttrImpl;
import edu.sc.seis.sod.model.event.OriginImpl;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.time.ClockUtil;

public class NetworkTimeRange implements OriginSubsetter, ArmListener,
        MicroSecondTimeRangeSupplier {

    public NetworkTimeRange() throws ConfigurationException {
        Start.add(this);
        // Don't get the network time range from the event arm!
        Start.getRunProps().setAllowDeadNets(true);
    }

    public void starting(Arm arm) throws ConfigurationException {
        if(!(arm instanceof NetworkArm)) {
            return;
        }
        this.arm = (NetworkArm)arm;
        this.arm.add(this);
    }

    public void started() throws ConfigurationException {
        if(arm == null) {
            throw new UserConfigurationException("Using a network time in the event arm requires a network arm");
        }
    }

    public void finished(Arm arm) {
        synchronized(finishLock) {
            finished = true;
            finishLock.notify();
        }
    }

    public StringTree accept(CacheEvent event,
                             EventAttrImpl eventAttr,
                             OriginImpl origin) {
        return new StringTreeLeaf(this,
                                  getMSTR().contains(origin.getOriginTime()));
    }

    public synchronized TimeRange getMSTR() {
        if(range != null) {
            return range;
        }
        synchronized(finishLock) {
            if(!finished) {
                try {
                    finishLock.wait();
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        List<Network> nets = arm.getSuccessfulNetworks();
        for (Network net : nets) {
            Station[] stas = arm.getSuccessfulStations(net);
            for(int j = 0; j < stas.length; j++) {
                List<Channel> chans = arm.getSuccessfulChannels(stas[j]);
                for(Channel c : chans) {
                    TimeRange chanRange = new TimeRange(c.getEffectiveTime());
                    if(range == null) {
                        range = chanRange;
                    } else {
                        if(chanRange.getBeginTime()
                                .isBefore(range.getBeginTime())) {
                            range = new TimeRange(chanRange.getBeginTime(),
                                                             range.getEndTime());
                        }
                        if(chanRange.getEndTime().isAfter(range.getEndTime())) {
                            range = new TimeRange(range.getBeginTime(),
                                                             chanRange.getEndTime());
                        }
                    }
                }
            }
        }
        if(range == null) {
            range = new TimeRange(ClockUtil.now(),
                                  ClockUtil.now());
        }
        return range;
    }

    private boolean finished;

    private NetworkArm arm;

    private Object finishLock = new Object();

    private TimeRange range;
}
