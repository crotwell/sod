package edu.sc.seis.sod.source.network;

import java.util.HashMap;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.hibernate.ChannelSensitivity;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;

public class InstrumentationFromDB extends WrappingNetworkSource implements NetworkSource {

    public InstrumentationFromDB(NetworkSource wrapped) {
        super(wrapped);
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        ChannelSensitivity dbSensitivity = NetworkDB.getSingleton().getSensitivity(chan);
        if (dbSensitivity != null) {
            if (!ChannelSensitivity.isNonChannelSensitivity(dbSensitivity)) {
                QuantityImpl out = new QuantityImpl(dbSensitivity.getOverallGain(), dbSensitivity.getInputUnits());
                return out;
            } else {
                // is in database, but marked as not existing, so
                throw new ChannelNotFound(chan.getId());
            }
        }
        // go to server?
        QuantityImpl sense = getWrapped().getSensitivity(chan);
        if (sense == null) {
            throw new ChannelNotFound(chan.getId());
        }
        dbSensitivity = new ChannelSensitivity(chan, (float)sense.getValue(), 0, sense.getUnit());
        NetworkDB.getSingleton().putSensitivity(dbSensitivity);
        return sense;
    }

    @Override
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound, SodSourceException {
        String key = ChannelIdUtil.toString(chan.getId());
        Instrumentation inst;
        try {
            synchronized(inProgress) {
                while (inProgress.containsKey(key)) {
                    try {
                        inProgress.wait();
                    } catch(InterruptedException e) {}
                }
                inProgress.put(key, "working");
                inProgress.notifyAll();
            }
            inst = NetworkDB.getSingleton().getInstrumentation(chan);
            if (inst != null && inst.the_response.stages.length == 0) {
                logger.warn("bad instrumentation in db, stages.length==0, regetting. "
                        + ChannelIdUtil.toStringNoDates(chan));
                NetworkDB.getSingleton().putInstrumentation(chan, null);
                inst = null;
            }
            if (inst == null) {
                // null means we have not yet tried to get this instrumentation
                // db throws ChannelNotFound if we tried before and got a
                // ChannelNotFound
                try {
                    inst = getWrapped().getInstrumentation(chan);
                    NetworkDB.getSingleton().putInstrumentation(chan, inst);
                } catch(ChannelNotFound e) {
                    logger.warn("exception", e);
                    NetworkDB.getSingleton().putInstrumentation(chan, null);
                } catch(InvalidResponse e) {
                    logger.warn("exception", e);
                    NetworkDB.getSingleton().putInstrumentation(chan, null);
                } catch(SodSourceException e) {
                    logger.warn("exception", e);
                    NetworkDB.getSingleton().putInstrumentation(chan, null);
                }
            }
        } finally {
            synchronized(inProgress) {
                inProgress.remove(key);
                inProgress.notifyAll();
            }
        }
        return inst;
    }
    
    private static HashMap<String, String> inProgress = new HashMap<String, String>();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InstrumentationFromDB.class);
}
