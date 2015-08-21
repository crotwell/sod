package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONWriter;


public abstract class AbstractJsonApiData implements JsonApiData {
    
    public AbstractJsonApiData(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
    }

    @Override
    public boolean hasRelationships() {
        return false;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
    }

    @Override
    public boolean hasLinks() {
        return false;
    }

    @Override
    public void encodeLinks(JSONWriter out) throws JSONException {
    }
      
    @Override
    public List<JsonApiData> included() {
        return new ArrayList<JsonApiData>();
    }

    public String getBaseUrl() {
        return baseUrl;
    }
    
    public static void doKeyValue(JSONWriter out, String key, Object value) {
        Matcher m = camelCasePattern.matcher(key);
        if (m.matches()) {
            throw new RuntimeException("Key looks like camelcase: "+key+" maybe you meant "+m.group(1)+"-"+m.group(2).toLowerCase()+m.group(3));
        }
        out.key(key).value(value);
    }
    

    static Pattern camelCasePattern = Pattern.compile("([a-z]+)([A-Z])(.*)");
    
    protected String baseUrl;
}
