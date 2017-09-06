package edu.sc.seis.sod.measure;

import java.time.Instant;

import org.json.JSONObject;


public class TimeRangeMeasurement extends Measurement {

    public TimeRangeMeasurement(String name, Instant start, Instant end) {
        super(name);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toXMLFragment() {
        return "<timeRange name=\"" + getName() + "\"><start>" + start.toString()+ "</start><end>" + end.toString() + "</end></timeRange>";
    }

    @Override
    public Object valueAsJSON() {
        JSONObject out = new JSONObject();
        out.append("start", start.toString());
        out.append("end", end.toString());
        return out;
    }
    
    Instant start, end;
}
