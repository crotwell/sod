package edu.sc.seis.sod.web.jsonapi;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.sod.AbstractEventChannelPair;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventVectorPair;

public class EventVectorJson extends AbstractJsonApiData {

    public EventVectorJson(AbstractEventChannelPair ecp, String baseUrl) {
        super(baseUrl);
        if (ecp == null) {
            throw new IllegalArgumentException("ECP can't be null");
        }
        this.ecp = ecp;
    }

    @Override
    public String getType() {
        return "event-vector";
    }

    @Override
    public String getId() {
        return "" + ecp.getDbid();
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("sod-status").value(ecp.getStatus());
        super.encodeAttributes(out);
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key("event").object();
        out.key("data").object();
        out.key("id").value("" + ecp.getEventDbId());
        out.key("type").value("event");
        out.endObject(); // end data
        out.endObject(); // end event

        out.key("channels").object();
        out.key("data").array();
        if (ecp instanceof EventChannelPair) {
            out.object();
            ChannelJson chanJson = new ChannelJson(((EventChannelPair)ecp).getChannel(), getBaseUrl());
            out.key("id").value(chanJson.getId());
            out.key("type").value(chanJson.getType());
            out.key("links").object();
            out.key("self").value(baseUrl + "/channel/" + chanJson.getId());
            out.endObject(); // end links
            out.endObject();
        } else {
            EventVectorPair evp = (EventVectorPair)ecp;
            ChannelImpl[] chans = evp.getChannelGroup().getChannels();
            for (int i = 0; i < chans.length; i++) {
                out.object();
                ChannelJson chanJson = new ChannelJson(chans[i], getBaseUrl());
                out.key("id").value(chanJson.getId());
                out.key("type").value(chanJson.getType());
                out.key("links").object();
                out.key("self").value(baseUrl + "/channel/" + chanJson.getId());
                out.endObject(); // end links
                out.endObject();
            }
        }
        out.endArray();
        out.endObject();

        out.key("waveform").object();
        out.key("data").object();
        out.key("id").value(getId());
        out.key("type").value("waveform");
        out.key("links").object();
        out.key("self").value(baseUrl + "/waveforms/" + getId());
        out.endObject();// links
        out.endObject();// data
        out.endObject();// ecps
    }

    AbstractEventChannelPair ecp;
}
