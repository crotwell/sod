package edu.sc.seis.sod.web.jsonapi.stationxml;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Comment;
import edu.sc.seis.seisFile.fdsnws.stationxml.Person;
import edu.sc.seis.seisFile.fdsnws.stationxml.StationXMLTagNames;
import edu.sc.seis.sod.web.jsonapi.ComplexAttributeJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class CommentJson implements ComplexAttributeJson {

    public static Comment decode(JSONObject res) {
        Comment c = new Comment(res.getString(StationXMLTagNames.VALUE));
        c.setSubject(res.optString(StationXMLTagNames.SUBJECT, null));
        c.setBeginEffectiveTime(res.optString(StationXMLTagNames.BEGINEFFECTIVETIME, null));
        c.setEndEffectiveTime(res.optString(StationXMLTagNames.ENDEFFECTIVETIME, null));
        if (res.optJSONArray(StationXMLTagNames.AUTHOR) != null) {
            for (Object o : res.optJSONArray(StationXMLTagNames.AUTHOR)) {
                if (o instanceof JSONObject) {
                    c.appendAuthor(PersonJson.decode((JSONObject)o));
                }
            }
        }
        return c;
    }
    
    public CommentJson(Comment comment) {
        this.comment = comment;
    }
    @Override
    public String getType() {
        return StationXMLTagNames.COMMENT;
    }

    public void encodeAttributes(JSONWriter out) throws JSONException {

        JsonApi.doKeyValue(out, StationXMLTagNames.SUBJECT, comment.getSubject());
        JsonApi.doKeyValue(out, StationXMLTagNames.VALUE, comment.getValue());
        JsonApi.doKeyValue(out, StationXMLTagNames.BEGINEFFECTIVETIME, comment.getBeginEffectiveTime());
        JsonApi.doKeyValue(out, StationXMLTagNames.ENDEFFECTIVETIME, comment.getEndEffectiveTime());
        out.key(StationXMLTagNames.AUTHOR).array();
        for (Person p : comment.getAuthorList()) {
            out.object();
            new PersonJson(p).encodeAttributes(out);
            out.endObject();
        }
        out.endArray();
    }

    
    Comment comment;
}
