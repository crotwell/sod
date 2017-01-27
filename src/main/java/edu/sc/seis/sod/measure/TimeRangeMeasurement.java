package edu.sc.seis.sod.measure;

import org.json.JSONObject;

import edu.iris.Fissures.model.MicroSecondDate;

public class TimeRangeMeasurement extends Measurement {

    public TimeRangeMeasurement(String name, MicroSecondDate start, MicroSecondDate end) {
        super(name);
        this.start = start;
        this.end = end;
    }

    @Override
    public String toXMLFragment() {
        return "<timeRange name=\"" + getName() + "\"><start>" + start.getFissuresTime().date_time+ "</start><end>" + end.getFissuresTime().date_time + "</end></timeRange>";
    }

    @Override
    public Object valueAsJSON() {
        JSONObject out = new JSONObject();
        out.append("start", start.getFissuresTime().date_time);
        out.append("end", end.getFissuresTime().date_time);
        return out;
    }
    
    MicroSecondDate start, end;
}
