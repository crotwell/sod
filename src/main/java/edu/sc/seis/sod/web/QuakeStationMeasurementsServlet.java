package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiDocument;
import edu.sc.seis.sod.web.jsonapi.JsonApiException;
import edu.sc.seis.sod.web.jsonapi.JsonApiResource;

public class QuakeStationMeasurementsServlet extends JsonToFileServlet {

    public QuakeStationMeasurementsServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "quakeStationMeasurements");
        idPattern = Pattern.compile(".*/quake-stations/([-_a-zA-Z0-9]+)/measurements");
        this.isArrayType = true;
    }

    @Override
    protected void updateAfterLoad(JsonApiDocument jsonApiDocument) throws IOException, JsonApiException {
        List<JsonApiResource> data = jsonApiDocument.getDataArray();
        JSONArray included = new JSONArray();
        MeasurementTextServlet mtServlet = new MeasurementTextServlet();
        for (JsonApiResource res : data) {
            JsonApiResource mt = mtServlet.load(res.getId());
            jsonApiDocument.include(mt);
        }
    }
    
}