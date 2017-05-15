package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class MeasurementTextServlet extends JsonToFileServlet {

    public MeasurementTextServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "measurement-texts");
        qsMeasurement = new QuakeStationMeasurementsServlet();
    }

    @Override
    protected void save(String id, JSONObject inJson) throws IOException {
        super.save(id, inJson);
        JSONObject relationships = inJson.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
        if (relationships.has("quake-station")) {
            String qsId = relationships.getJSONObject("quake-station").getJSONObject(JsonApi.DATA).getString(JsonApi.ID);
            JSONObject qsRel = qsMeasurement.load(qsId);
            JSONArray knownMeas = qsRel.getJSONArray(JsonApi.DATA);
            boolean found = false;
            for (int i = 0; i < knownMeas.length(); i++) {
                if (knownMeas.getJSONObject(i).getString(JsonApi.ID).equals(id)) {
                    //found so done
                    found = true;
                    break;
                }
            }
            if ( ! found) {
                JSONObject idItem = new JSONObject();
                idItem.put(JsonApi.ID, id);
                idItem.put(JsonApi.TYPE, jsonType);
                knownMeas.put(idItem);
                qsMeasurement.save(qsId, qsRel);
            }

            logger.debug("after save knownMeas from qs="+qsId+"  len="+knownMeas.length());
        } else {
            throw new JSONException("relationship missing "+inJson.toString(2));
        }
    }
    
    QuakeStationMeasurementsServlet qsMeasurement;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MeasurementTextServlet.class);
}
