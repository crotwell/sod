package edu.sc.seis.sod.web.jsonapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

public class JsonApi {

    public static final String DETAIL = "detail";
    public static final String ERRORS = "errors";
    public static final String RELATIONSHIPS = "relationships";
    public static final String LINKS = "links";
    public static final String META = "meta";
    public static final String ATTRIBUTES = "attributes";
    public static final String TYPE = "type";
    public static final String ID = "id";
    public static final String INCLUDED = "included";
    public static final String DATA = "data";
    public static final String SELF = "self";
	public static final String RELATED = "related";

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

    public static String loadFromReader(BufferedReader in) throws IOException {
        StringBuffer json = new StringBuffer();
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = in.read(buf)) != -1) {
            json.append(String.valueOf(buf, 0, numRead));
        }
        return json.toString();
    }
    
    public static boolean hasData(JSONObject json) {
    	return json.has(DATA);
    }
    
    public static boolean hasRelationships(JSONObject json) {
    	return json.has(RELATIONSHIPS);
    }
    
    public static boolean hasLinks(JSONObject json) {
    	return json.has(LINKS);
    }
    
    public static boolean hasIncluded(JSONObject json) {
    	return json.has(INCLUDED);
    }
    
    public static JsonApiDocument decode(JSONObject json) throws JsonApiException {
    	JsonApiDocument out = new JsonApiDocument(json);
    	return out;
    }

    public static void doKeyValue(JSONWriter out, String key, Object value) {
        Matcher m = JsonApi.camelCasePattern.matcher(key);
        if (m.matches()) {
            throw new RuntimeException("Key looks like camelcase: "+key+" maybe you meant "+m.group(1)+"-"+m.group(2).toLowerCase()+m.group(3));
        }
        if (value != null) {
            out.key(key).value(value);
        }
    }

    static Pattern camelCasePattern = Pattern.compile("([a-z]+)([A-Z])(.*)");
}
