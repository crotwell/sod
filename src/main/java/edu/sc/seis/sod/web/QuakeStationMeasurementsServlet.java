package edu.sc.seis.sod.web;

import java.io.File;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class QuakeStationMeasurementsServlet extends JsonToFileServlet {

    public QuakeStationMeasurementsServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "quakeStationMeasurements");
        idPattern = Pattern.compile(".*/quake-stations/([-_a-zA-Z0-9]+)/measurements");
        empty = new JSONObject();
        empty.put(JsonApi.DATA, new JSONArray());
    }

    @Override
    protected JSONObject createEmpty(String id) {
        return empty;
    }
    
    JSONObject empty;
}