package edu.sc.seis.sod.web.jsonapi.stationxml;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.ExternalReference;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.sod.web.jsonapi.ComplexAttributeJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class ExternalReferenceJson  implements ComplexAttributeJson {

    public static ExternalReference decode(JSONObject res) {
        return new ExternalReference(res.getString(StationXMLTagNames.URI), res.getString(StationXMLTagNames.DESCRIPTION));
    }

    public ExternalReferenceJson(ExternalReference externalRef) {
        this.externalRef = externalRef;
    }

    ExternalReference externalRef;
    
    @Override
    public String getType() {
        return StationXMLTagNames.EXTERNALREFERENCE;
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        JsonApi.doKeyValue(out, StationXMLTagNames.SUBJECT, externalRef.getUri());
        JsonApi.doKeyValue(out, StationXMLTagNames.DESCRIPTION, externalRef.getDescription());
    }

    
}
