package edu.sc.seis.sod.hibernate;

import edu.iris.Fissures.Time;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;

/** caches Instrumentation within the database, avoiding a call on the server if it is requested a second time.
 * 
 * @author crotwell
 * 
 * Created on Feb 23, 2010
 */
public class InstrumentationDBNetworkAccess extends CacheNetworkAccess {

    public InstrumentationDBNetworkAccess(NetworkAccess net) {
        super(net);
    }
    
    @Override
    public Instrumentation retrieve_instrumentation(ChannelId id, Time theTime) throws ChannelNotFound {
        try {
            ChannelImpl chan = NetworkDB.getSingleton().getChannel(id);
            Instrumentation inst = NetworkDB.getSingleton().getInstrumentation(chan);
            if (inst != null) {return inst;
            } else {
                try {
                    inst = super.retrieve_instrumentation(id, theTime);
                    NetworkDB.getSingleton().putInstrumentation(chan, inst);
                    return inst;
                } catch (ChannelNotFound ee) {
                    // not found from server, put null in db
                    NetworkDB.getSingleton().putInstrumentation(chan, null);
                    throw ee;
                }
            }
        } catch(NotFound e) {
            // channel not in db, get from server
            return super.retrieve_instrumentation(id, theTime);
        }
        
    }
    
    
}
