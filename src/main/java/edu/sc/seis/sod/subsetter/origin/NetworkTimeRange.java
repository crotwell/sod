package edu.sc.seis.sod.subsetter.origin;

import java.util.List;

import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.Arm;
import edu.sc.seis.sod.ArmListener;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

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
                             EventAttr eventAttr,
                             Origin origin) {
        return new StringTreeLeaf(this,
                                  getMSTR().contains(new MicroSecondDate(origin.getOriginTime())));
    }

    public synchronized MicroSecondTimeRange getMSTR() {
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
        List<CacheNetworkAccess> nets = arm.getSuccessfulNetworks();
        for (CacheNetworkAccess net : nets) {
            StationImpl[] stas = arm.getSuccessfulStations(net.get_attributes());
            for(int j = 0; j < stas.length; j++) {
                List<ChannelImpl> chans = arm.getSuccessfulChannels(stas[j]);
                for(ChannelImpl c : chans) {
                    MicroSecondTimeRange chanRange = new MicroSecondTimeRange(c.getEffectiveTime());
                    if(range == null) {
                        range = chanRange;
                    } else {
                        if(chanRange.getBeginTime()
                                .before(range.getBeginTime())) {
                            range = new MicroSecondTimeRange(chanRange.getBeginTime(),
                                                             range.getEndTime());
                        }
                        if(chanRange.getEndTime().after(range.getEndTime())) {
                            range = new MicroSecondTimeRange(range.getBeginTime(),
                                                             chanRange.getEndTime());
                        }
                    }
                }
            }
        }
        if(range == null) {
            range = new MicroSecondTimeRange(new MicroSecondDate(),
                                             new MicroSecondDate());
        }
        return range;
    }

    private boolean finished;

    private NetworkArm arm;

    private Object finishLock = new Object();

    private MicroSecondTimeRange range;
}
