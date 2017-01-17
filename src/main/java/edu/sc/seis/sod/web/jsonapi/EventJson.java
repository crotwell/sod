package edu.sc.seis.sod.web.jsonapi;

import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.event.OriginImpl;
import edu.sc.seis.sod.hibernate.StatefulEvent;

public class EventJson extends AbstractJsonApiData {

    public EventJson(StatefulEvent event, String baseUrl) {
        super(baseUrl);
        this.event = event;
    }

    @Override
    public String getType() {
        return "quake";
    }

    @Override
    public String getId() {
        return "" + event.getDbid();
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("name").value(event.get_attributes().name).key("sod-status").value(event.getStatus().toString());
        if (numSuccessfulStations != null) {
            out.key("num-successful-stations").value(numSuccessfulStations);
        }
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        try {
            OriginImpl pref = event.getPreferred();
            out.key("pref-origin").object();
            out.key("data")
                    .object()
                    .key("id")
                    .value("" + event.getPreferred().getDbid())
                    .key("type")
                    .value("origin")
                    .endObject(); // end data
            out.endObject(); // end pref-origin
            out.key("pref-magnitude").object();
            out.key("data")
                    .object()
                    .key("id")
                    .value(createMagId(event.getPreferred(), 0))
                    .key("type")
                    .value("magnitude")
                    .endObject(); // end data
            out.endObject(); // end pref-magnitude
            out.key("esps").object()
            .key("links").object()
            .key("related")
            .value(formStationRelationshipURL());
    out.endObject(); // end links
    out.endObject(); // end events
        } catch(NoPreferredOrigin e) {
            // should never happen
            throw new RuntimeException("no pref origin???", e);
        }
        /*
         * out.key("origins") .object() .key("links") .object() .key("self")
         * .value(formOriginRelationshipURL(event)) .key("related")
         * .value(formOriginListURL(event)); out.endObject(); // end links
         * out.endObject(); // end origins out.key("magnitudes") .object()
         * .key("links") .object() .key("self")
         * .value(formMagnitudeRelationshipURL(event)) .key("related")
         * .value(formMagnitudeListURL(event));
         */
    }

    @Override
    public boolean hasLinks() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void encodeLinks(JSONWriter out) throws JSONException {
        // TODO Auto-generated method stub
    }

    @Override
    public List<JsonApiData> included() {
        List<JsonApiData> out = super.included();
        try {
            out.add(new OriginJson(event.getPreferred(), getBaseUrl()));
            out.add(new MagnitudeJson(event.getPreferred().getMagnitudes()[0],
                                      createMagId(event.getPreferred(), 0),
                                      getBaseUrl()));
        } catch(NoPreferredOrigin e) {
            // should never happen
            throw new RuntimeException(e);
        }
        return out;
    }
    
    

    public static String createMagId(OriginImpl o, int magIndex) {
        return o.getDbid() + "m" + magIndex;
    }

    public String formStationRelationshipURL() {
        String out = baseUrl+"/quakes/"+getId()+"/stations";
        return out;
        
    }
    
    public Integer getNumSuccessfulStations() {
        return numSuccessfulStations;
    }
    
    public void setNumSuccessfulStations(Integer numSuccessful) {
        this.numSuccessfulStations = numSuccessful;
    }

    StatefulEvent event;
    
    Integer numSuccessfulStations = null;
}
