package edu.sc.seis.sod.velocity.network;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.InvalidResponse;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;


public class VelocityInstrumentation {
    
    /** lazily loaded instrumentation for velocity templates. */
    public VelocityInstrumentation(NetworkSource source, Channel chan) {
        this.source = source;
        this.chan = chan;
    }
    
    public String toString() {
        return getSensitivity()+" "+getSensorModel();
    }
    
    void checkLoadInstrumentation() {
        if ( ! didTryLoad) {
            didTryLoad = true;
            try {
                chan.setResponse(source.getResponse(chan));
            } catch(ChannelNotFound e) {
            } catch(SodSourceException e) {
            } catch(edu.sc.seis.seisFile.fdsnws.stationxml.InvalidResponse e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public VelocitySensitivity getSensitivity() {
        if (this.chan.getResponse() != null && this.chan.getResponse().getInstrumentSensitivity() != null) {
            return new VelocitySensitivity(this.chan.getResponse().getInstrumentSensitivity());
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
    Channel chan;
}
