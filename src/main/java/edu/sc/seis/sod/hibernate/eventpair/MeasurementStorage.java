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
    
    AbstractEventChannelPair ecp;
    EventStationPair esp;
    
    /** sometime you need an empty cookie jar...*/
    public MeasurementStorage() {
        measurements = new JSONObject();
    }

    public MeasurementStorage(EventStationPair esp, JSONObject measurements) {
        this.esp = esp;
        this.measurements = measurements;
    }

    public MeasurementStorage(AbstractEventChannelPair ecp, JSONObject measurements) {
        this.ecp = ecp;
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
    
    public String getAsString(String key) {
        return measurements.getString(key);
    }
    
    public JSONObject getAsJSONObject(String key) {
        return measurements.getJSONObject(key);
    }
    
    public double getAsDouble(String key) {
        return measurements.getDouble(key);
    }
    
    public int getAsInt(String key) {
        return measurements.getInt(key);
    }
    
    public Object getRaw(String key) {
        return measurements.get(key);
    }
    
    public JSONObject getAll() {
        return measurements;
    }
    
    public AbstractEventChannelPair getECP() {
        return ecp;
    }
    
    public EventStationPair getESP() {
        return esp;
    }
    
}// CookieJar
