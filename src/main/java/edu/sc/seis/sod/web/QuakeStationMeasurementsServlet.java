package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class QuakeStationMeasurementsServlet extends JsonToFileServlet {

    public QuakeStationMeasurementsServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "quakeStationMeasurements");
        idPattern = Pattern.compile(".*/quake-stations/([-_a-zA-Z0-9]+)/measurements");
        
    }

    @Override
    protected JSONObject load(String id) throws IOException {
        // TODO Auto-generated method stub
        JSONObject out = super.load(id);
        JSONArray data = out.getJSONArray(JsonApi.DATA);
        JSONArray included = new JSONArray();
        MeasurementTextServlet mtServlet = new MeasurementTextServlet();
        for (int i = 0; i < data.length(); i++) {
            JSONObject mt = mtServlet.load(data.getJSONObject(i).getString(JsonApi.ID)).getJSONObject(JsonApi.DATA);
            included.put(mt);
        }
        if (included.length() > 0) {
            out.put(JsonApi.INCLUDED, included);
        }
        return out;
    }

    @Override
    protected void save(String id, JSONObject inJson) throws IOException {
        inJson.remove(JsonApi.INCLUDED); // don't save included measurements in this as it is just a relationship
        super.save(id, inJson);
    }

    @Override
    protected JSONObject createEmpty(String id) {
        JSONObject empty = new JSONObject();
        empty.put(JsonApi.DATA, new JSONArray());
        return empty;
    }
    
    
}