package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.sod.EventStationPair;

public class EventStationJson extends AbstractJsonApiData {

    public EventStationJson(EventStationPair esp, String baseUrl) {
        super(baseUrl);
        this.esp = esp;
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
        out.key("event").object();
        out.key("data")
                .object()
                .key("id")
                .value("" + esp.getEventDbId())
                .key("type")
                .value("event")
                .endObject(); // end data
        out.endObject(); // end event
        out.key("station").object();
        out.key("data")
                .object()
                .key("id")
                .value(esp.getStationDbId())
                .key("type")
                .value("station")
                .endObject(); // end data
        out.endObject(); // end station
    }

    @Override
    public String getType() {
        return "eventstation";
    }

    @Override
    public String getId() {
        return "" + esp.getDbid();
    }

    EventStationPair esp;
}
