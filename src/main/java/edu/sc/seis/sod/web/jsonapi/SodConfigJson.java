package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.sod.SodConfig;
import edu.sc.seis.sod.model.common.MicroSecondDate;

public class SodConfigJson  extends AbstractJsonApiData {

    public SodConfigJson(SodConfig config, String baseUrl) {
        super(baseUrl);
        this.config = config;
    }


    @Override
    public String getType() {
        return "sodConfig";
    }

    @Override
    public String getId() {
        String s = ""+config.getDbid();
        return s;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("timestamp").value(new MicroSecondDate(config.getTime()).getISOString());
        out.key("config").value(config.getConfig());
    }
    
    SodConfig config;
}
