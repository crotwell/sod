package edu.sc.seis.sod.hibernate.eventpair;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * CookieJar exists as a way for various subsetters and processors in the
 * waveform arm to pass information down the chain. It is implemented as a
 * Velocity Context which allows the cusomization of output status pages through
 * velocity template files. The Event and Channel are placed in the context with
 * names "sod_event" and "sod_channel". Created: Thu Dec 13 18:18:48 2001
 * 
 * @author Philip Crotwell
 */
public class MeasurementStorage  {
    
    JSONObject measurements = null;
    
    /** sometime you need an empty cookie jar...*/
    public MeasurementStorage() {
        measurements = new JSONObject();
    }

    public MeasurementStorage(JSONObject measurements) {
        this.measurements = measurements;
    }


    public JSONObject getMeasurements() {
        return measurements;
    }
    
    public void addMeasurement(String key, String val) {
        if (getMeasurements() == null) {
            measurements = new JSONObject();
        }
        measurements.put(key, val);
    }
    
    public void addMeasurement(String key, int val) {
        if (getMeasurements() == null) {
            measurements = new JSONObject();
        }
        measurements.put(key, val);
    }
    
    public void addMeasurement(String key, float val) {
        if (getMeasurements() == null) {
            measurements = new JSONObject();
        }
        measurements.put(key, val);
    }
    
    public void addMeasurement(String key, double val) {
        if (getMeasurements() == null) {
            measurements = new JSONObject();
        }
        measurements.put(key, val);
    }
    
    public void addMeasurement(String key, JSONObject val) {
        if (getMeasurements() == null) {
            measurements = new JSONObject();
        }
        measurements.put(key, val);
    }
    
    public void addMeasurement(String key, JSONArray val) {
        if (getMeasurements() == null) {
            measurements = new JSONObject();
        }
        measurements.put(key, val);
    }
    
    
    
    
    
    
    
}// CookieJar
