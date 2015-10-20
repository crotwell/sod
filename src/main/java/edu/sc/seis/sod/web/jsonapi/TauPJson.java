package edu.sc.seis.sod.web.jsonapi;

import java.util.List;

import org.json.JSONWriter;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.BuildVersion;
import edu.sc.seis.TauP.TauP_Time;

/**
 * Output travel time arrivals into JSON format. 
*/
public class TauPJson {

    public TauPJson(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void encode(List<Arrival> arrivalList, JSONWriter out) {
        out.object();
        out.key("arrivals").array();
        for (Arrival arrival : arrivalList) {
            out.object();
            out.key("phasename").value(arrival.getPhase().getName());
            out.key("puristname").value(arrival.getPhase().getPuristName());
            out.key("traveltime").value(arrival.getTime());
            out.key("distdeg").value(arrival.getDistDeg());
            out.key("distrad").value(arrival.getDist());
            out.key("sourcedepth").value(arrival.getSourceDepth());
            out.key("incidentangle").value(arrival.getIncidentAngle());
            out.key("takeoffangle").value(arrival.getTakeoffAngle());
            out.key("rayparamrad").value(arrival.getRayParam());
            out.key("rayparamdeg").value(arrival.getRayParamDeg());
            out.key("model").value(arrival.getPhase().getTauModel().getModelName());
            out.endObject();
        }
        out.endArray();
        out.key("serviceurl").value(baseUrl);
        out.key("version").value(BuildVersion.getDetailedVersion());
        out.endObject();
    }
    
    String baseUrl;
}
