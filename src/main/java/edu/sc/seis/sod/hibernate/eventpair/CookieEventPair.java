package edu.sc.seis.sod.hibernate.eventpair;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.sc.seis.sod.measure.Measurement;
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
    // hibernate
    protected void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, String> getCookies() {
        return cookies;
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
        this.measurements = new MeasurementStorage(new JSONObject(val));
    }
    
    
    private MeasurementStorage measurements;
}
