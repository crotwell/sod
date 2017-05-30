package edu.sc.seis.sod.measure;

import org.json.JSONObject;

import edu.sc.seis.sod.model.common.MicroSecondDate;


public class TimeRangeMeasurement extends Measurement {

    public TimeRangeMeasurement(String name, MicroSecondDate start, MicroSecondDate end) {
        super(name);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toXMLFragment() {
        return "<timeRange name=\"" + getName() + "\"><start>" + start.getISOString()+ "</start><end>" + end.getISOString() + "</end></timeRange>";
    }

    @Override
    public Object valueAsJSON() {
        JSONObject out = new JSONObject();
        out.append("start", start.getISOString());
        out.append("end", end.getISOString());
        return out;
    }
    
    MicroSecondDate start, end;
}
