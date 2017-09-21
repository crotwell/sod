package edu.sc.seis.sod.hibernate.eventpair;

import org.json.JSONObject;

import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Status;

public abstract class CookieEventPair extends AbstractEventPair {

    public CookieEventPair() {
        super();
    }

    public CookieEventPair(StatefulEvent event, Status status) {
        super(event, status);
    }

    public CookieEventPair(StatefulEvent event) {
        super(event);
    }

    protected void setStatus(Status status) {
        super.setStatus(status);
    }
    
    public MeasurementStorage getMeasurements() {
        return measurements;
    }
    
    protected String getMeasurementsStr() {
        if (measurements == null) {
            return null;
        }
        return measurements.toString();
    }
    protected void setMeasurementsStr(String val) {
        if (this instanceof AbstractEventChannelPair) {
            this.measurements = new MeasurementStorage((AbstractEventChannelPair)this, new JSONObject(val));
        } else if (this instanceof EventStationPair) {
            this.measurements = new MeasurementStorage((EventStationPair)this, new JSONObject(val));
        }
        throw new RuntimeException("SHould be esp or ecp, but was: "+this.getClass());
    }
    
    
    private MeasurementStorage measurements;
}
