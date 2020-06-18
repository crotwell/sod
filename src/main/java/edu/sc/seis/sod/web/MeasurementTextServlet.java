package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiException;
import edu.sc.seis.sod.web.jsonapi.JsonApiResource;

public class MeasurementTextServlet extends JsonToFileServlet {

    public MeasurementTextServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "measurements");
        qsMeasurement = new QuakeStationMeasurementsServlet();
    }

    @Override
    protected void save(JsonApiResource inJson) throws IOException {
    	try {
        super.save(inJson);
        if (inJson.hasRelationship("quake-station")) {
            String qsId = inJson.getRelationship("quake-station").getData().getId();
            JSONArray knownMeas = qsMeasurement.loadArray(qsId);
            boolean found = false;
            for (Iterator iterator = knownMeas.iterator(); iterator.hasNext();) {
				Object subObj = iterator.next();
				if (subObj instanceof JSONObject) {
					JsonApiResource res = new JsonApiResource((JSONObject)subObj);
					if (res.getId().equals(inJson.getId())) {
	                    //found so done
	                    found = true;
	                    break;
	                }
				} else {
					throw new JsonApiException("Array contains non-resource object: "+subObj);
				}
			}
            if ( ! found) {
            	JsonApiResource res = new JsonApiResource(inJson.getId(), jsonType);
                knownMeas.put(res.getWrapped());
                qsMeasurement.save(qsId, knownMeas);
            }

            logger.debug("after save knownMeas from qs="+qsId+"  len="+knownMeas.length());
        } else {
            throw new JSONException("relationship missing "+inJson.toString(2));
        }
    	} catch(JsonApiException e) {
    		throw new IOException(e);
    	}
    }
    
    QuakeStationMeasurementsServlet qsMeasurement;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MeasurementTextServlet.class);
}
