package edu.sc.seis.sod.web.jsonapi;

import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;


public interface JsonApiData {
    
    public String getType();
    
    public String getId();
    
    public void encodeAttributes(JSONWriter out) throws JSONException;
    
    public boolean hasRelationships();
    
    public void encodeRelationships(JSONWriter out) throws JSONException;
    
    public boolean hasLinks();
    
    public void encodeLinks(JSONWriter out) throws JSONException;
    
    public List<JsonApiData> included();
    
}
