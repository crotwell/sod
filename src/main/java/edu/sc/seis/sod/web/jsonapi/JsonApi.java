package edu.sc.seis.sod.web.jsonapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public class JsonApi {

    public static final String DETAIL = "detail";
    public static final String ERRORS = "errors";
    public static final String RELATIONSHIPS = "relationships";
    public static final String LINKS = "links";
    public static final String ATTRIBUTES = "attributes";
    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String INCLUDED = "included";
    public static final String DATA = "data";

    public static void encodeJson(JSONWriter out, JsonApiData data) throws JSONException {
        out.object();
        out.key(DATA).object();
        encodeInner(out, data);
        out.endObject();
        List<JsonApiData> include = data.included();
        if (include.size() > 0) {
            out.key(INCLUDED).array();
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
        out.key(ID).value(data.getId());
        out.key(TYPE).value(data.getType());
        out.key(ATTRIBUTES).object();
        data.encodeAttributes(out);
        out.endObject();
        if(data.hasLinks()) {
            out.key(LINKS).object();
            data.encodeLinks(out);
            out.endObject();
        }
        if(data.hasRelationships()) {
            out.key(RELATIONSHIPS).object();
            data.encodeRelationships(out);
            out.endObject();
        }
    }

    public static void encodeJsonWithoutInclude(JSONWriter out, List<JsonApiData> dataList) throws JSONException {
        out.object();
        internalEncodeJsonWithoutInclude(out, dataList);
        out.endObject();
    }
    
    protected static void internalEncodeJsonWithoutInclude(JSONWriter out, List<JsonApiData> dataList) throws JSONException {
        out.key(DATA).array();
        for (JsonApiData jsonApiData : dataList) {
            out.object();
            out.key(ID).value(jsonApiData.getId());
            out.key(TYPE).value(jsonApiData.getType());
            out.endObject();
        }
        out.endArray();
    }
    
    public static void encodeJson(JSONWriter out, List<JsonApiData> dataList) throws JSONException {
        out.object();
        
        internalEncodeJsonWithoutInclude(out, dataList);
        
        List<JsonApiData> toInclude = new ArrayList<JsonApiData>();
        toInclude.addAll(dataList);
        for (JsonApiData jsonApiData : dataList) {
            toInclude.addAll(jsonApiData.included());
        }

        out.key(INCLUDED).array();
        for (JsonApiData jsonApiData : toInclude) {
            out.object();
            encodeInner(out, jsonApiData);
            out.endObject();
        }
        out.endArray();
        out.endObject();
    }
    
    public static void encodeError(JSONWriter out, String message) {
        System.err.println("JsonApi.Error: "+message);
        out.object().key(ERRORS).array().object().key(DETAIL).value(message).endObject().endArray().endObject();
    }

    public static JSONObject loadFromReader(BufferedReader in) throws IOException {
        StringBuffer json = new StringBuffer();
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = in.read(buf)) != -1) {
            json.append(String.valueOf(buf, 0, numRead));
        }
        JSONObject out = new JSONObject(json.toString());
        return out;
    }
}
