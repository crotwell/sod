package edu.sc.seis.sod.source.network;

import java.util.HashMap;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.hibernate.ChannelSensitivity;
import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;

public class InstrumentationFromDB extends WrappingNetworkSource implements NetworkSource {

    public InstrumentationFromDB(NetworkSource wrapped) {
        super(wrapped);
    }

    @Override
    public QuantityImpl getSensitivity(Channel chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        ChannelSensitivity dbSensitivity = NetworkDB.getSingleton().getSensitivity(chan);
        if (dbSensitivity != null) {
            if (!ChannelSensitivity.isNonChannelSensitivity(dbSensitivity)) {
                QuantityImpl out = new QuantityImpl(dbSensitivity.getOverallGain(), dbSensitivity.getInputUnits());
                return out;
            } else {
                // is in database, but marked as not existing, so
                throw new ChannelNotFound(chan);
            }
        }
        // go to server?
        QuantityImpl sense = getWrapped().getSensitivity(chan);
        if (sense == null) {
            throw new ChannelNotFound(chan);
        }
        dbSensitivity = new ChannelSensitivity(chan, (float)sense.getValue(), 0, sense.getUnit());
        NetworkDB.getSingleton().putSensitivity(dbSensitivity);
        return sense;
    }

    @Override
    public Response getResponse(Channel chan) throws ChannelNotFound, SodSourceException {
        String key = ChannelIdUtil.toString(chan);
        Response inst;
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
            inst = NetworkDB.getSingleton().getResponse(chan);
            if ( ! Response.isValid(inst)) {
                logger.warn("bad instrumentation in db, regetting. "
                        + ChannelIdUtil.toStringNoDates(chan));
                NetworkDB.getSingleton().putResponse(chan, null);
                inst = null;
            }
            if (inst == null) {
                // null means we have not yet tried to get this instrumentation
                // db throws ChannelNotFound if we tried before and got a
                // ChannelNotFound
                try {
                    inst = getWrapped().getResponse(chan);
                    NetworkDB.getSingleton().putResponse(chan, inst);
                } catch(ChannelNotFound e) {
                    logger.warn("exception", e);
                    NetworkDB.getSingleton().putResponse(chan, null);
                } catch(InvalidResponse e) {
                    logger.warn("exception", e);
                    NetworkDB.getSingleton().putResponse(chan, null);
                } catch(SodSourceException e) {
                    logger.warn("exception", e);
                    NetworkDB.getSingleton().putResponse(chan, null);
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
