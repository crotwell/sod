package edu.sc.seis.sod.web.jsonapi;

import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.sod.AbstractEventChannelPair;
import edu.sc.seis.sod.EventStationPair;

public class EventStationJson extends AbstractJsonApiData {

    public EventStationJson(EventStationPair esp, String baseUrl) {
        this(esp, null, baseUrl);
    }

    public EventStationJson(EventStationPair esp, List<AbstractEventChannelPair> ecpList, String baseUrl) {
        super(baseUrl);
        this.esp = esp;
        this.ecpList = ecpList;
    }

    @Override
    public String getId() {
        return "" + esp.getDbid();
    }

    @Override
    public String getType() {
        return "quake-station";
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("sod-status").value(esp.getStatus().toString());
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key("quake").object();
        out.key("data").object();
        out.key("id").value("" + esp.getEventDbId());
        out.key("type").value("quake");
        out.key("links").object();
        out.key("self").value(baseUrl + "/quakes/" + esp.getEventDbId());
        out.endObject(); // end links
        out.endObject(); // end data
        out.endObject(); // end event
        out.key("station").object();
        out.key("data").object();
        out.key("id").value(new StationJson(esp.getStation(), getBaseUrl()).getId());
        out.key("type").value("station");
        out.key("links").object();
        out.key("self").value(baseUrl + "/stations/" +  esp.getStationDbId());
        out.endObject(); // end links
        out.endObject(); // end data
        out.endObject(); // end station
        if (ecpList != null) {
            out.key("ecps");
            out.object();
            out.key("data").array();
            for (AbstractEventChannelPair ecp : ecpList) {
                out.object();
                out.key("id").value(ecp.getDbid());
                out.key("type").value("quake-vector");
                out.endObject();
            }
            out.endArray();
            out.endObject();
        } else {
            out.key("ecps").object();
            out.key("links").object();
            out.key("related").value(baseUrl + "/quake-stations/" + getId() + "/quake-vectors");
            out.endObject();// links
            out.endObject();// ecps
        }
    }

    @Override
    public List<JsonApiData> included() {
        List<JsonApiData> out = super.included();
        if (ecpList != null) {
            for (AbstractEventChannelPair ecp : ecpList) {
                out.add(new EventVectorJson(ecp, getBaseUrl()));
            }
        }
        return out;
    }

    EventStationPair esp;

    List<AbstractEventChannelPair> ecpList;
}
