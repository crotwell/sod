package edu.sc.seis.sod.web.jsonapi.stationxml;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Equipment;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.sod.web.jsonapi.ComplexAttributeJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class EquipmentJson implements ComplexAttributeJson {

    public static Equipment decode(JSONObject res) {
        Equipment equip = new Equipment();
        equip.setType(res.optString(StationXMLTagNames.TYPE));
        equip.setResourceId(res.optString(StationXMLTagNames.RESOURCEID));
        equip.setDescription(res.optString(StationXMLTagNames.DESCRIPTION));
        equip.setManufacturer(res.optString(StationXMLTagNames.MANUFACTURER));
        equip.setVendor(res.optString(StationXMLTagNames.VENDOR));
        equip.setModel(res.optString(StationXMLTagNames.MODEL));
        equip.setSerialNumber(res.optString(StationXMLTagNames.SERIALNUMBER));
        equip.setInstallationDate(res.optString(StationXMLTagNames.INSTALLATIONDATE));
        equip.setRemovalDate(res.optString(StationXMLTagNames.REMOVALDATE));
        for (Object o : res.getJSONArray(StationXMLTagNames.CALIBRATIONDATE)) {
            if (o != null && o instanceof String) {
                equip.appendCalibrationDate((String)o);
            }
        }
        return equip;
    }
    
    public EquipmentJson(Equipment equip) {
        this.equip = equip;
    }
    
    Equipment equip;
    
    @Override
    public String getType() {
        return "equipment";
    }

    @Override
    public void encodeAttributes(JSONWriter out) throws JSONException {
        JsonApi.doKeyValue(out, StationXMLTagNames.TYPE, equip.getType());
        JsonApi.doKeyValue(out, StationXMLTagNames.RESOURCEID, equip.getResourceId());
        JsonApi.doKeyValue(out, StationXMLTagNames.DESCRIPTION, equip.getDescription());
        JsonApi.doKeyValue(out, StationXMLTagNames.MANUFACTURER, equip.getManufacturer());
        JsonApi.doKeyValue(out, StationXMLTagNames.VENDOR, equip.getVendor());
        JsonApi.doKeyValue(out, StationXMLTagNames.MODEL, equip.getModel());
        JsonApi.doKeyValue(out, StationXMLTagNames.SERIALNUMBER, equip.getSerialNumber());
        JsonApi.doKeyValue(out, StationXMLTagNames.INSTALLATIONDATE, equip.getInstallationDate());
        JsonApi.doKeyValue(out, StationXMLTagNames.REMOVALDATE, equip.getRemovalDate());
        if (equip.getCalibrationDate().size() != 0) {
            out.key(StationXMLTagNames.CALIBRATIONDATE).array();
            for (String c : equip.getCalibrationDate()) {
                out.value(c);
            }
            out.endArray();
        }
        
    }

}
