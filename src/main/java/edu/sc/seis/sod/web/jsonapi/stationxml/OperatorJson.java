package edu.sc.seis.sod.web.jsonapi.stationxml;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Operator;
import edu.sc.seis.seisFile.fdsnws.stationxml.Person;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.sod.web.jsonapi.ComplexAttributeJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class OperatorJson  implements ComplexAttributeJson {


    public static Operator decode(JSONObject res) {
        Operator operator = new Operator(res.optString(StationXMLTagNames.AGENCY));
        for (Object o : res.getJSONArray(StationXMLTagNames.CONTACT)) {
            if (o != null && o instanceof JSONObject) {
                operator.appendContact(PersonJson.decode((JSONObject)o));
            }
        }
        operator.setWebsite(res.optString(StationXMLTagNames.WEBSITE));
        return operator;
    }
    
    public OperatorJson(Operator operator) {
        this.operator = operator;
    }
    
    Operator operator;
    
    @Override
    public String getType() {
        return "operator";
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        JsonApi.doKeyValue(out, StationXMLTagNames.AGENCY,operator.getAgency());
        out.key(StationXMLTagNames.CONTACT).array();
        for (Person p : operator.getContactList()) {
            out.object();
            new PersonJson(p).encodeAttributes(out);
            out.endObject();
        }
        out.endArray();
    }

}
