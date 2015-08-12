package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

public class JsonApi {

    public static void encodeJson(JSONWriter out, JsonApiData data) throws JSONException {
        out.object();
        out.key("data").object();
        encodeInner(out, data);
        out.endObject();
        List<JsonApiData> include = data.included();
        if (include.size() > 0) {
            out.key("included").array();
            for (JsonApiData jsonApiData : include) {
                out.object();
                encodeInner(out, jsonApiData);
                out.endObject();
            }
            out.endArray();
        }
        out.endObject();
    }
    
    static void encodeInner(JSONWriter out, JsonApiData data) {
        out.key("id").value(data.getId());
        out.key("type").value(data.getType());
        out.key("attributes").object();
        data.encodeAttributes(out);
        out.endObject();
        if(data.hasLinks()) {
            out.key("links").object();
            data.encodeLinks(out);
            out.endObject();
        }
        if(data.hasRelationships()) {
            out.key("relationships").object();
            data.encodeRelationships(out);
            out.endObject();
        }
    }

    public static void encodeJson(JSONWriter out, List<JsonApiData> dataList) throws JSONException {
        out.object();
        out.key("data").array();
        for (JsonApiData jsonApiData : dataList) {
            out.object();
            out.key("id").value(jsonApiData.getId());
            out.key("type").value(jsonApiData.getType());
            out.endObject();
        }
        out.endArray();
        
        List<JsonApiData> toInclude = new ArrayList<JsonApiData>();
        toInclude.addAll(dataList);
        for (JsonApiData jsonApiData : dataList) {
            toInclude.addAll(jsonApiData.included());
        }

        out.key("included").array();
        for (JsonApiData jsonApiData : toInclude) {
            out.object();
            encodeInner(out, jsonApiData);
            out.endObject();
        }
        out.endArray();
        out.endObject();
    }
}
