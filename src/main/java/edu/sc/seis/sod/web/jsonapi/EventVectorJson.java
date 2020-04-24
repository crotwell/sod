package edu.sc.seis.sod.web.jsonapi;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.eventpair.AbstractEventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.measure.Measurement;
import edu.sc.seis.sod.process.waveform.AbstractSeismogramWriter;

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
        return "quake-vector";
    }

    @Override
    public String getId() {
        return "" + ecp.getDbid();
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        out.key("sod-status").value(ecp.getStatus());
        JSONObject measurements = new JSONObject();
        for (String key : ecp.getCookieJar().getAll().keySet()) {
            if (key.startsWith(AbstractSeismogramWriter.COOKIE_PREFIX)) {
                // skip local storage files as we don't want to leak these outside of the application
                continue;
            }
            String jsonFriendlyKey = key.replaceAll("\\W", "-");
            measurements.put(jsonFriendlyKey, ecp.getCookieJar().getRaw(key));
        }
        super.encodeAttributes(out);
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key("quake").object();
        out.key("data").object();
        out.key("id").value("" + ecp.getEventDbId());
        out.key("type").value("quake");
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
            out.key("self").value(baseUrl + "/channels/" + chanJson.getId());
            out.endObject(); // end links
            out.endObject();
        } else {
            EventVectorPair evp = (EventVectorPair)ecp;
            Channel[] chans = evp.getChannelGroup().getChannels();
            for (int i = 0; i < chans.length; i++) {
                out.object();
                ChannelJson chanJson = new ChannelJson(chans[i], getBaseUrl());
                out.key("id").value(chanJson.getId());
                out.key("type").value(chanJson.getType());
                out.key("links").object();
                out.key("self").value(baseUrl + "/channels/" + chanJson.getId());
                out.endObject(); // end links
                out.endObject();
            }
        }
        out.endArray();
        out.endObject(); //channels

        out.key("waveforms").object();
        out.key("links").object();
        out.key("related").value(baseUrl + "/quake-vectors/"  + getId() + "/waveforms");
        out.endObject();// links
        out.endObject();// waveforms
    }

    AbstractEventChannelPair ecp;
}
