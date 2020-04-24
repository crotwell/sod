package edu.sc.seis.sod.web.jsonapi;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.EventSeismogramFileReference;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.hibernate.UnsupportedFileTypeException;

public class WaveformJson extends AbstractJsonApiData {
    
    public WaveformJson(EventSeismogramFileReference ref, String baseUrl) {
        super(baseUrl);
        this.ref = ref;
    }

    @Override
    public String getType() {
        return "waveform";
    }

    @Override
    public String getId() {
        return ""+ref.getDbid();
    }
    
    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
    	out.key("network-code").value(ref.getNetworkCode());
    	out.key("station-code").value(ref.getStationCode());
    	out.key("loc-code").value(ref.getLocCode());
    	out.key("channel-code").value(ref.getChannelCode());
    	out.key("begin-time").value(TimeUtils.toISOString(ref.getBeginTime()));
    	out.key("end-time").value(TimeUtils.toISOString(ref.getEndTime()));
    	out.key("file-path").value(ref.getFilePath());
    	out.key("data-url").value(formDataURL(ref));
    	try {
			out.key("file-type").value(SeismogramFileTypes.fromInt(ref.getFileType()).getName());
		} catch (UnsupportedFileTypeException e) {
			out.key("file-type").value(""+ref.getFileType());
		}
    }
    

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        out.key("quake").object();
        out.key("data").object();
        out.key("id").value("" + ref.getEvent().getDbid());
        out.key("type").value("quake");
        out.endObject(); // end data
        out.endObject(); // end event
        
    }

    EventSeismogramFileReference ref;

    public String formDataURL(EventSeismogramFileReference ref) {
        String out = baseUrl+"/waveforms/"+ref.getDbid()+"/mseed";
        return out;
    }


    public static List<JsonApiData> toJsonList(List<EventSeismogramFileReference> refList, String baseUrl) {
        List<JsonApiData> out = new ArrayList<JsonApiData>(refList.size());
        for (EventSeismogramFileReference ref : refList) {
            out.add(new WaveformJson(ref, baseUrl));
        }
        return out;
    }
}