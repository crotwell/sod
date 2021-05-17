package edu.sc.seis.sod.web.jsonapi.stationxml;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Person;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.sod.web.jsonapi.ComplexAttributeJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class PersonJson implements ComplexAttributeJson {

    public static Person decode(JSONObject json) {
        Person p = new Person();
        p.setName(json.getString(StationXMLTagNames.NAME));
        if (json.optJSONArray(StationXMLTagNames.AGENCY) != null) {
            for (Object o : json.optJSONArray(StationXMLTagNames.AGENCY)) {
                p.appendAgency((String)o);
            }
        }
        if (json.optJSONArray(StationXMLTagNames.PHONE) != null) {
            for (Object o : json.optJSONArray(StationXMLTagNames.PHONE)) {
                p.appendPhone((String)o);
            }
        }
        if (json.optJSONArray(StationXMLTagNames.EMAIL) != null) {
            for (Object o : json.optJSONArray(StationXMLTagNames.EMAIL)) {
                p.appendEmail((String)o);
            }
        }
        return p;
    }
    
    public PersonJson(Person person) {
        this.person = person;
    }
    
    @Override
    public String getType() {
        return "person";
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        JsonApi.doKeyValue(out, StationXMLTagNames.NAME, person.getName());
        if (person.getAgencyList().size() != 0) {
            out.key(StationXMLTagNames.AGENCY).array();
            for (String s : person.getAgencyList()) {
                out.value(s);
            }
            out.endArray();
        }
        if (person.getPhoneList().size() != 0) {
            out.key(StationXMLTagNames.PHONE).array();
            for (String s : person.getPhoneList()) {
                out.value(s);
            }
            out.endArray();
        }
        if (person.getEmailList().size() != 0) {
            out.key(StationXMLTagNames.EMAIL).array();
            for (String s : person.getEmailList()) {
                out.value(s);
            }
            out.endArray();
        }
        
    }

    Person person;
}
