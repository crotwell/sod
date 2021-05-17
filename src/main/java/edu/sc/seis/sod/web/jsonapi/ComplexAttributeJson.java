package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public interface ComplexAttributeJson {


    public String getType();
    
    public void encodeAttributes(JSONWriter out) throws JSONException;
    
    public static List<JSONObject> allObjectsOfName(JSONObject json, String name) {
        List<JSONObject> out = new ArrayList<JSONObject>();
        if (json.optJSONArray(name) != null) {
            for (Object o : json.optJSONArray(name)) {
                if (o instanceof JSONObject) {
                    out.add((JSONObject)o);
                }
            }
        }
        return out;
    }
    
}
