package edu.sc.seis.sod.web.jsonapi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.sc.seis.sod.web.JsonToFileServlet;

public class JsonApiDocument {
	
	public static JsonApiDocument parse(String json) throws JSONException, JsonApiException {
        JsonApiDocument doc = new JsonApiDocument(new JSONObject(json.toString()));
		if ( ! doc.hasData()) {
			throw new JsonApiException("data missing in document: "+json);
		}
        return doc;
	}
	
	public static JsonApiDocument createEmpty() {
		JSONObject obj = new JSONObject();
        JsonApiDocument out;
		try {
			out = new JsonApiDocument(obj);
		} catch (JsonApiException e) {
			throw new RuntimeException("can't happen, but it did", e);
		}
        return out;
		
	}

	
	public static JsonApiDocument createEmptyArray() {
		JsonApiDocument out = createEmpty();
		out.setDataArray(new ArrayList<JsonApiResource>());
        return out;
		
	}
	
	public static JsonApiDocument createForResource(JsonApiResource res) {
		JsonApiDocument doc = createEmpty();
		doc.setData(res);
		return doc;
	}
	
	public static JsonApiDocument createForArray(JSONArray arr) throws JSONException, JsonApiException {
		JsonApiDocument doc = createEmptyArray();
		for (Object object : arr) {
			if (object instanceof JSONObject) {
				doc.append(new JsonApiResource((JSONObject)object));
			} else {
				throw new JsonApiException("Expected JSONObject but found "+object.getClass().getName());
			}
		}
		return doc;
	}

	JsonApiDocument(JSONObject jsonObject) throws JsonApiException {
		if (jsonObject == null) {
			throw new JsonApiException("jsonObject cannot be null");
		}
		this.wrapped = jsonObject;
		if (hasData() && ! isDataArray()) {
			// check if new and needs ID created
			JsonApiResource data = getData();
			if (data.needIdCreated()) {
				data.createId();
			} else {
				logger.debug("Id already there, not created: "+data.getId());
			}
		} else {
			logger.debug("no data or array, so skip id check");
		}
	}

	public boolean hasData() throws JsonApiException {
		boolean out = this.wrapped.optJSONArray(JsonApi.DATA) != null;
		if ( ! out && this.wrapped.optJSONObject(JsonApi.DATA) != null) {
			out = ! this.wrapped.isNull(JsonApi.DATA);
		}
		return out;
	}
	
	public boolean isDataArray() {
		return this.wrapped.opt(JsonApi.DATA) instanceof JSONArray;
	}
	
	public JsonApiResource getData() throws JsonApiException {
		Object obj = this.wrapped.opt(JsonApi.DATA);
		if (obj != null && ! this.wrapped.isNull(JsonApi.DATA) && obj instanceof JSONObject) {
			return new JsonApiResource((JSONObject)obj);
		} else {
			return null;
		}
	}
	
	public void setData(JsonApiResource value) {
		this.wrapped.put(JsonApi.DATA, value.getWrapped());
	}
	
	public List<JsonApiResource> getDataArray() throws JsonApiException {
		Object obj = this.wrapped.opt(JsonApi.DATA);
		if (obj != null && obj instanceof JSONArray) {
			JSONArray arr = (JSONArray)obj;
			List<JsonApiResource> out = new ArrayList<JsonApiResource>();
			for (Object o : arr) {
				out.add(new JsonApiResource((JSONObject)o));
			}
			return out;
		} else {
			return null;
		}
	}

	public void setDataArray(List<JsonApiResource> value) {
		JSONArray arr = new JSONArray();
		for (JsonApiResource res : value) {
			arr.put(res.getWrapped());
		}
		this.wrapped.put(JsonApi.DATA, arr);
	}
	
	public void append(JsonApiResource value) throws JSONException, JsonApiException {
		if ( ! hasData()) {
			this.wrapped.put(JsonApi.DATA, new JSONArray());
		}
		if (isDataArray()) {
			((JSONArray)this.wrapped.get(JsonApi.DATA)).put(value.wrapped);
		} else {
			throw new JsonApiException("data is not array");
		}
	}
	
	public boolean hasIncluded() throws JsonApiException {
		return this.wrapped.optJSONArray(JsonApi.INCLUDED) != null;
	}
	
	public void include(JsonApiResource value) throws JSONException, JsonApiException {
		if ( ! hasIncluded()) {
			this.wrapped.put(JsonApi.INCLUDED, new JSONArray());
		}
		((JSONArray)this.wrapped.get(JsonApi.INCLUDED)).put(value.getWrapped());
	}

	JSONObject wrapped;


	public String toString(int i) {
		return this.wrapped.toString(i);
	}

	public String toString() {
		return this.wrapped.toString();
	}

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JsonApiDocument.class);
}
