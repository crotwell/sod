package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.sod.model.event.Magnitude;



public class MagnitudeJson extends AbstractJsonApiData {

    public MagnitudeJson(Magnitude mag, String magId, String baseUrl) {
        super(baseUrl);
        this.mag = mag;
        this.magId = magId;
    }
    
    @Override
    public String getType() {
        return "magnitudes";
    }

    @Override
    public String getId() {
        return magId;
    }
    
    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("value").value(mag.value)
        .key("mag-type").value(mag.type)
        .key("contributor").value(mag.contributor);
    }
    
    Magnitude mag;
    String magId;
}
