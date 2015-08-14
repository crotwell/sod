package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.sod.Arm;


public class ArmStatusJson extends AbstractJsonApiData {
    
    public ArmStatusJson(Arm arm, String baseUrl) {
        super(baseUrl);
        this.arm = arm;
    }

    @Override
    public String getType() {
        return "arm";
    }

    @Override
    public String getId() {
        return arm.getName();
    }
    
    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("name").value(arm.getName())
        .key("is-active").value(arm.isActive());
    }

    Arm arm;
}
