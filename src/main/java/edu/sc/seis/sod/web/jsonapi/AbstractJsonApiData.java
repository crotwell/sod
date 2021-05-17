package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.List;

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
    
    protected String baseUrl;
}
