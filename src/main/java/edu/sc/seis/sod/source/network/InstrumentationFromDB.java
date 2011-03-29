package edu.sc.seis.sod.source.network;

import java.util.HashMap;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.ChannelSensitivity;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;

public class InstrumentationFromDB extends WrappingNetworkSource implements NetworkSource {

    public InstrumentationFromDB(NetworkSource wrapped) {
        super(wrapped);
    }
    
    @Override
    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        try {
            return getInstrumentation(NetworkDB.getSingleton().getChannel(chanId));
        } catch(NotFound e) {
            throw new ChannelNotFound(chanId);
        }
    }

    @Override
    public QuantityImpl getSensitivity(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        String key = ChannelIdUtil.toStringNoDates(chanId);
        if (sensitivityMap.containsKey(key)) {
            return sensitivityMap.get(key);
        }
        ChannelImpl chan;
        try {
            chan = NetworkDB.getSingleton().getChannel(chanId);
            ChannelSensitivity dbSensitivity = NetworkDB.getSingleton().getSensitivity(chan);
            if (dbSensitivity != null) {
                QuantityImpl out = new QuantityImpl(dbSensitivity.getOverallGain(), dbSensitivity.getInputUnits());
                sensitivityMap.put(key, out);
                return out;
            }
            QuantityImpl sense = getWrapped().getSensitivity(chanId);
            sensitivityMap.put(key, sense);
            dbSensitivity = new ChannelSensitivity(chan, (float)sense.getValue(), 0, sense.getUnit());
            NetworkDB.getSingleton().putSensitivity(dbSensitivity);
            return sense;
        } catch(NotFound e) {
            // must not be in db
            throw new ChannelNotFound(chanId);
        }
        // should probalby try to get from db, maybe convert from instrumentation like:
        // return new QuantityImpl(inst.the_response.the_sensitivity.sensitivity_factor,
        //                         inst.the_response.stages[0].input_units);
    }
    
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound {
        Instrumentation inst = NetworkDB.getSingleton().getInstrumentation(chan);
        if (inst != null && inst.the_response.stages.length == 0) {
            logger.warn("bad instrumentation in db, stages.length==0, regetting. "+ChannelIdUtil.toStringNoDates(chan));
            NetworkDB.getSingleton().putInstrumentation(chan, null);
            inst = null;
        }
        if (inst == null ) {
            // null means we have not yet tried to get this instrumentation
            // db throws ChannelNotFound if we tried before and got a ChannelNotFound
            try {
                inst = getWrapped().getInstrumentation(chan.getId());
                NetworkDB.getSingleton().putInstrumentation(chan, inst);
            } catch (ChannelNotFound e) {
                logger.warn("exception", e);
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            } catch(InvalidResponse e) {
                logger.warn("exception", e);
                NetworkDB.getSingleton().putInstrumentation(chan, null);
            }
        }
        return inst;
    }
    
    private HashMap<String, QuantityImpl> sensitivityMap = new HashMap<String, QuantityImpl>();
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InstrumentationFromDB.class);
    
}
