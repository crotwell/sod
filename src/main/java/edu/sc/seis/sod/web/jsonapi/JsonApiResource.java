package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.sc.seis.sod.web.JsonToFileServlet;

public class JsonApiResource {


	public static List<JsonApiResource> parseArray(String json) throws JSONException, JsonApiException {
		List<JsonApiResource> out = new ArrayList<JsonApiResource>();
		JSONArray jsonArr = new JSONArray(json);
		for (Object obj : jsonArr) {
			if (obj instanceof JSONObject) {
				JsonApiResource res = new JsonApiResource((JSONObject)obj);
				out.add(res);
			}
		}
        return out;
	}

	public static JsonApiResource parse(String json) throws JSONException, JsonApiException {
		JsonApiResource doc = new JsonApiResource(new JSONObject(json));
        return doc;
	}

	public JsonApiResource(JSONObject jsonObject) throws JsonApiException {
		this.wrapped = jsonObject;
		if (getType() == null) {
			throw new JsonApiException("type missing in resource");
		}
	}

	public JsonApiResource(String id, String type) throws JsonApiException {
		this(new JSONObject().put(JsonApi.ID, id).put(JsonApi.TYPE, type));
	}
	
	public String createId() {
		this.wrapped.put(JsonApi.ID, java.util.UUID.randomUUID().toString());
		logger.info("Create id: "+getId());
		return this.getId();
	}

	public JSONObject rawAttributeObject() {
		JSONObject attr = this.wrapped.optJSONObject(JsonApi.ATTRIBUTES);
		return attr;
	}
	
	public Iterator<String> attributeKeys() {
		JSONObject obj = rawAttributeObject();
		if (obj == null) {
			// empty iterator
			return new JSONObject().keys();
		}
		return obj.keys();
	}

	public boolean hasAttribute(String key) {
		return getAttribute(key) != null;
	}
	
	public Object getAttribute(String key) {
		JSONObject attr = rawAttributeObject();
		Object out = null;
		if (attr != null) {
			out = attr.opt(key);
		}
		return out;
	}
	
	public String getAttributeString(String key) {
		JSONObject attr = rawAttributeObject();
		String out = null;
		if (attr != null) {
			out = attr.optString(key);
		}
		return out;
	}
	
	public String getAttributeString(String key, String defaultValue) {
		String out = getAttributeString(key);
		return out != null ? out : defaultValue;
	}
	
	public Integer getAttributeInt(String key) {
		JSONObject attr = rawAttributeObject();
		Integer out = null;
		if (attr != null) {
			out = attr.optInt(key);
		}
		return out;
	}
	
	public Integer getAttributeInt(String key, Integer defaultValue) {
		Integer out = getAttributeInt(key);
		return out != null ? out : defaultValue;
	}
	
	public Double getAttributeDouble(String key) {
		JSONObject attr = rawAttributeObject();
		Double out = null;
		if (attr != null) {
			out = attr.optDouble(key);
		}
		return out;
	}
	
	public Double getAttributeDouble(String key, Double defaultValue) {
		Double out = getAttributeDouble(key);
		return out != null ? out : defaultValue;
	}
    
    public JSONObject getAttributeJsonObject(String key) {
        JSONObject attr = rawAttributeObject();
        JSONObject out = null;
        if (attr != null) {
            out = attr.optJSONObject(key);
        }
        return out;
    }
	
	public JSONArray getAttributeArray(String key) {
		JSONObject attr = rawAttributeObject();
		JSONArray out = null;
		if (attr != null) {
			out = attr.optJSONArray(key);
		}
		return out;
	}
	
	public void setAttribute(String key, Object value) {
		if (this.rawAttributeObject() == null) {
			this.wrapped.put(JsonApi.ATTRIBUTES, new JSONObject());
		}
		this.wrapped.getJSONObject(JsonApi.ATTRIBUTES).put(key, value);
	}

	public JSONObject rawRelationshipObject() {
		JSONObject rel = this.wrapped.optJSONObject(JsonApi.RELATIONSHIPS);
		return rel;
	}
	
	public Iterator<String> relationshipKeys() {
		JSONObject obj = rawRelationshipObject();
		if (obj == null) {
			// empty iterator
			return new JSONObject().keys();
		}
		return obj.keys();
	}
	
	public boolean hasRelationship(String key) {
		boolean out = rawRelationshipObject().has(key);
		if (out) {
			Object obj = rawRelationshipObject().get(key);
			if (obj instanceof JSONObject) {
			    out = ! rawRelationshipObject().getJSONObject(key).isNull(JsonApi.DATA);
			} else {
				JSONArray arr = rawRelationshipObject().getJSONArray(key);
				out = true;
			}
		}
		return out;
	}
	
	public JsonApiDocument getRelationship(String key) throws JsonApiException {
		JSONObject rel = rawRelationshipObject();
		JsonApiDocument out = null;
		if (hasRelationship(key)) {
			logger.debug("getRelationship("+key+")  "+ (rel.optJSONObject(key)));
			if (rel.optJSONObject(key) != null) {
				out = new JsonApiDocument(rel.optJSONObject(key));
			} else if (rel.optJSONArray(key) != null) {
				out = JsonApiDocument.createForArray(rel.optJSONArray(key));
			} else {
				throw new JsonApiException("Relationship must be either JSONObject or JSONArray: "+rel.opt(key));
			}
		}
		return out;
	}
	
	public void setRelationshipNull(String key) {
		JSONObject related = new JSONObject();
		
		related.put(JsonApi.DATA, JSONObject.NULL);
		setRelationshipRaw(key, related);
	}
	
	public void setRelationship(String key, String id, String type) {
		JSONObject related = new JSONObject();
		JSONObject data = new JSONObject();
		data.put(JsonApi.ID, id);
		data.put(JsonApi.TYPE, type);
		related.put(JsonApi.DATA, data);
		setRelationshipRaw(key, related);
	}

	
	public void appendRelationship(String key, String id, String type) throws JSONException, JsonApiException {
		if ( ! hasRelationship(key)) {
			JSONObject related = new JSONObject();
			JSONArray data = new JSONArray();
			related.put(JsonApi.DATA, data);
			setRelationshipRaw(key, related);
		}
		JSONArray relArr = getRelationshipArrayRaw(key);
		if ( relArr == null) {
			throw new JsonApiException("Attempt to append to "+key+" but related is not array");
		}
		relArr.put(new JsonApiResource(id, type).getWrapped());
	}
	
	public void setRelationshipLink(String key, String relatedUrl, String selfUrl) {
		JSONObject related = new JSONObject();
		JSONObject links = new JSONObject();
		if (selfUrl != null) { links.put(JsonApi.SELF, selfUrl); }
		if (relatedUrl != null) { links.put(JsonApi.RELATED, relatedUrl); }
		related.put(JsonApi.LINKS, links);
		setRelationshipRaw(key, related);
	}
	
	public JSONObject getRelationshipRaw(String key) {
		if (this.rawRelationshipObject() == null) {
			return null;
		}
		return this.wrapped.getJSONObject(JsonApi.RELATIONSHIPS).optJSONObject(key);
	}
	
	public JSONArray getRelationshipArrayRaw(String key) {
		JSONObject rel = getRelationshipRaw(key);
		if (rel == null) {
			return null;
		}
		return rel.optJSONArray(JsonApi.DATA);
	}
	
	public void setRelationshipRaw(String key, Object value) {
		if (this.rawRelationshipObject() == null) {
			this.wrapped.put(JsonApi.RELATIONSHIPS, new JSONObject());
		}
		this.wrapped.getJSONObject(JsonApi.RELATIONSHIPS).put(key, value);
	}

	public void deleteRelationship(String key) {
		if (this.rawRelationshipObject() == null) {
			return;
		}
		this.wrapped.getJSONObject(JsonApi.RELATIONSHIPS).remove(key);
	}

	public JSONObject getLinks() {
		JSONObject rel = this.wrapped.optJSONObject(JsonApi.LINKS);
		return rel;
	}
	
	public JSONObject getLink(String key) {
		JSONObject rel = getLinks();
		JSONObject out = null;
		if (rel != null) {
			out = rel.optJSONObject(key);
		}
		return out;
	}
	
	public void setLink(String key, String value) {
		if (this.getLinks() == null) {
			this.wrapped.put(JsonApi.LINKS, new JSONObject());
		}
		this.wrapped.getJSONObject(JsonApi.LINKS).put(key, value);
	}

	public JSONObject getMetaObj() {
		JSONObject rel = this.wrapped.optJSONObject(JsonApi.LINKS);
		return rel;
	}
	
	public JSONObject getMeta(String key) {
		JSONObject rel = getMetaObj();
		JSONObject out = null;
		if (rel != null) {
			out = rel.optJSONObject(key);
		}
		return out;
	}
	
	public void setMeta(String key, JSONObject value) {
		if (this.getMetaObj() == null) {
			this.wrapped.put(JsonApi.META, new JSONObject());
		}
		this.wrapped.getJSONObject(JsonApi.META).put(key, value);
	}
	
	public boolean needIdCreated() {
		return this.wrapped.optString(JsonApi.ID).length() == 0;
	}
	
	public String optId() {
		return this.wrapped.optString(JsonApi.ID);
	}
	
	public String getId() {
		return this.wrapped.getString(JsonApi.ID);
	}
	
	public String getType() {
		return this.wrapped.getString(JsonApi.TYPE);
	}

	public JSONObject getWrapped() {
		return wrapped;
	}
	
	public String toString(int i) {
		return this.wrapped.toString(i);
	}

	JSONObject wrapped;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JsonApiResource.class);

}
