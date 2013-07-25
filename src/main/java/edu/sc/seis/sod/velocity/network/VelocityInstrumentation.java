package edu.sc.seis.sod.velocity.network;

import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;


public class VelocityInstrumentation {
    
    /** lazily loaded instrumentation for velocity templates. */
    public VelocityInstrumentation(NetworkSource source, ChannelImpl chan) {
        this.source = source;
        this.chan = chan;
    }
    
    void checkLoadInstrumentation() {
        if ( ! didTryLoad) {
            didTryLoad = true;
            try {
                inst = source.getInstrumentation(chan);
            } catch(ChannelNotFound e) {
            } catch(InvalidResponse e) {
            } catch(SodSourceException e) {
            }
        }
    }
    
    public VelocitySensitivity getSensitivity() {
        checkLoadInstrumentation();
        if (inst != null) {
            return new VelocitySensitivity(inst.the_response.the_sensitivity);
        } else {
            return null;
        }
    }

    
    public String getSensorModel() {
        checkLoadInstrumentation();
        if (inst != null) {
            return inst.the_sensor.model;
        } else {
            return "unknown";
        }
    }
    
    public String getDataLoggerModel() {
        checkLoadInstrumentation();
        if (inst != null) {
            return inst.das.model;
        } else {
            return "unknown";
        }
    }
    
    boolean didTryLoad = false;
    Instrumentation inst = null;
    NetworkSource source;
    ChannelImpl chan;
}
