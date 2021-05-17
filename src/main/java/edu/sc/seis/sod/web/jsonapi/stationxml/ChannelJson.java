package edu.sc.seis.sod.web.jsonapi.stationxml;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Equipment;
import edu.sc.seis.seisFile.fdsnws.stationxml.ExternalReference;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.seisFile.fdsnws.stationxml.Unit;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.web.jsonapi.AbstractJsonApiData;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiResource;


public class ChannelJson extends AbstractJsonApiData {

    public static Channel decode(JsonApiResource res) {
        Channel chan = new Channel();
        NetworkJson.decodeBaseNodeAttributes(res, chan);
        chan.setLocCode(res.getAttributeString(StationXMLTagNames.LOC_CODE));
        chan.setSampleRate(res.getAttributeDouble(StationXMLTagNames.SAMPLE_RATE).floatValue());
        chan.setLatitude(res.getAttributeDouble(StationXMLTagNames.LAT).floatValue());
        chan.setLongitude(res.getAttributeDouble(StationXMLTagNames.LON).floatValue());
        chan.setElevation(res.getAttributeDouble(StationXMLTagNames.ELEVATION).floatValue());
        chan.setDepth(res.getAttributeDouble(StationXMLTagNames.DEPTH).floatValue());
        chan.setAzimuth(res.getAttributeDouble(StationXMLTagNames.AZIMUTH).floatValue());
        chan.setDip(res.getAttributeDouble(StationXMLTagNames.DIP).floatValue());
        chan.setWaterlevel(res.getAttributeDouble(StationXMLTagNames.WATERLEVEL).floatValue());
        if (res.hasAttribute(StationXMLTagNames.SENSOR)) {
            chan.setSensor(EquipmentJson.decode(res.getAttributeJsonObject(StationXMLTagNames.SENSOR)));
        }
        if (res.hasAttribute(StationXMLTagNames.PREAMPLIFIER)) {
            chan.setPreAmplifier(EquipmentJson.decode(res.getAttributeJsonObject(StationXMLTagNames.PREAMPLIFIER)));
        }
        if (res.hasAttribute(StationXMLTagNames.DATALOGGER)) {
            chan.setDataLogger(EquipmentJson.decode(res.getAttributeJsonObject(StationXMLTagNames.DATALOGGER)));
        }
        for (Object e : res.getAttributeArray(StationXMLTagNames.EQUIPMENT)) {
            if (e instanceof JSONObject) {
                chan.appendEquipment(EquipmentJson.decode((JSONObject)e));
            }
        }
        
        chan.setClockDrift(res.getAttributeDouble(StationXMLTagNames.CLOCK_DRIFT).floatValue());
        chan.setCalibrationUnits(new Unit(res.getAttributeString(StationXMLTagNames.CALIBRATIONUNITS)));
        return chan;
    }
    
    public ChannelJson(Channel chan, String baseUrl) {
        super(baseUrl);
        this.chan = chan;
        if (chan.getDbid() == 0) {
            
        }
    }
    
    @Override
    public String getType() {
        return "channels";
    }

    @Override
    public String getId() {
        if (chan.getDbid() == 0) {       
            StationJson staJson = new StationJson(chan.getStation(), baseUrl);
            return staJson.getId()+"_"+chan.getLocCode()+"_"+chan.getCode()+"_"+TimeUtils.toCompactISOString(chan.getStartDateTime());
        }
        return ""+chan.getDbid();
   }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        JsonApi.doKeyValue(out, StationXMLTagNames.LOC_CODE, chan.getLocCode());
        NetworkJson.encodeBaseNodeAttributes(out, chan);
        
        JsonApi.doKeyValue(out, StationXMLTagNames.SAMPLE_RATE, chan.getSampleRate().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.LAT, chan.getLatitude().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.LON, chan.getLongitude().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.ELEVATION, chan.getElevation().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.DEPTH, chan.getDepth().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.AZIMUTH, chan.getAzimuth().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.DIP, chan.getDip().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.WATERLEVEL, chan.getWaterlevel().getValue());
        if (chan.getTypeList().size() != 0) {
            out.key(StationXMLTagNames.TYPE).array();
            for (String t: chan.getTypeList()) {
                out.value(t);
            }
            out.endArray();
        }
        
        JsonApi.doKeyValue(out, StationXMLTagNames.CLOCK_DRIFT, chan.getClockDrift().getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.CALIBRATIONUNITS, chan.getCalibrationUnits());
        if (chan.getExternalReferenceList().size() != 0) {
            out.key(StationXMLTagNames.EXTERNALREFERENCE).array();
            for (ExternalReference c : chan.getExternalReferenceList()) {
                out.object();
                new ExternalReferenceJson(c).encodeAttributes(out);
                out.endObject();
            }
            out.endArray();
        }
        if (chan.getSensor() != null) {
            out.key(StationXMLTagNames.SENSOR).object();
            new EquipmentJson(chan.getSensor()).encodeAttributes(out);
            out.endObject();
        }
        if (chan.getPreAmplifier() != null) {
            out.key(StationXMLTagNames.PREAMPLIFIER).object();
            new EquipmentJson(chan.getPreAmplifier()).encodeAttributes(out);
            out.endObject();
        }
        if (chan.getDataLogger() != null) {
            out.key(StationXMLTagNames.DATALOGGER).object();
            new EquipmentJson(chan.getDataLogger()).encodeAttributes(out);
            out.endObject();
        }
        if (chan.getEquipmentList().size() != 0) {
            out.key(StationXMLTagNames.EQUIPMENT).array();
            for (Equipment c : chan.getEquipmentList()) {
                out.object();
                new EquipmentJson(c).encodeAttributes(out);
                out.endObject();
            }
            out.endArray();
        }
    }

    @Override
    public boolean hasRelationships() {
        return true;
    }

    @Override
    public void encodeRelationships(JSONWriter out) throws JSONException {
        StationJson staJson = new StationJson(chan.getStation(), getBaseUrl());
        out.key("station").object();
        out.key("id").value(staJson.getId());
        out.key("type").value(staJson.getType());
        out.endObject();
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
    
    Channel chan;
}
