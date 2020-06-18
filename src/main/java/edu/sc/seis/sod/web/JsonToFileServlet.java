package edu.sc.seis.sod.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiDocument;
import edu.sc.seis.sod.web.jsonapi.JsonApiException;
import edu.sc.seis.sod.web.jsonapi.JsonApiResource;

public abstract class JsonToFileServlet extends HttpServlet {

    public JsonToFileServlet(String baseUrl, File baseDir, String jsonType) {
        this.baseUrl = baseUrl;
        this.baseDir = baseDir;
        this.jsonType = jsonType;
        allPattern = Pattern.compile(".*/"+jsonType);
        idPattern = Pattern.compile(".*/"+jsonType+"/([-_a-zA-Z0-9]+)");
        jsonDir = new File(baseDir, jsonType);
        if (!jsonDir.exists()) {
            jsonDir.mkdirs();
            logger.info("Create json Dir: " + jsonDir.getAbsolutePath());
        }
        this.isArrayType = false;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            System.out.println("GET: " + URL);
            logger.info("GET: " + URL);
            WebAdmin.setJsonHeader(req, resp);
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            Matcher matcher = allPattern.matcher(URL);
            if (matcher.matches()) {
                // all json file ids
                List<String> jsonlIds = getAllJsonIds();
                JsonApiDocument doc = JsonApiDocument.createEmptyArray();
                for (String pId : jsonlIds) {
                    JsonApiResource res;
					try {
						res = new JsonApiResource(pId, jsonType);
						//res.setLink("self", this.baseUrl+"/"+this.jsonType+"/"+pId );
	                    doc.append(res);
					} catch (JsonApiException e) {
						JsonApi.encodeError(out, "Error in saved document: id="+pId+" type="+jsonType+" url:" + URL);
						writer.close();
						resp.sendError(204);
					}
                }
                writer.write(doc.toString(2));
            } else {
                matcher = idPattern.matcher(URL);
                if (matcher.matches()) {
                    String pId = matcher.group(1);
                    try {
						if (this.isArrayType && ! exists(pId)) {
							JsonApiDocument p = JsonApiDocument.createEmptyArray();
							updateAfterLoad(p);
							writer.print(p.toString(2));
						} else if (this.isArrayType) {
							JSONArray jsonArr = loadArray(pId);
							JsonApiDocument p = JsonApiDocument.createForArray(jsonArr);
							updateAfterLoad(p);
							writer.print(p.toString(2));
						} else {
							JsonApiResource res = load(pId);
							JsonApiDocument p = JsonApiDocument.createForResource(res);
							updateAfterLoad(p);
							writer.print(p.toString(2));
						}
					} catch (FileNotFoundException e) {
						JsonApi.encodeError(out, "Not Found: id="+pId+" type="+jsonType+" url:" + URL);
						writer.close();
						resp.sendError(204);
					} catch (JsonApiException e) {
						logger.warn("Error in saved document: id="+pId+" type="+jsonType+" url:" + URL);
						JsonApi.encodeError(out, "Error in saved document: id="+pId+" type="+jsonType+" url:" + URL);
						writer.close();
						resp.sendError(204);
					}
                } else {
                    logger.warn("Bad URL for servlet: type="+jsonType+" url:" + URL);
                    JsonApi.encodeError(out, "bad url for servlet: type="+jsonType+" url:" + URL);
                    writer.close();
                    resp.sendError(500);
                }
            }
            writer.close();
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(NumberFormatException e) {
            throw new ServletException(e);
        } finally {
            NetworkDB.rollback();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	String rawJson = null;
        try {
        WebAdmin.setJsonHeader(req, resp);
        String URL = req.getRequestURL().toString();
        rawJson = JsonApi.loadFromReader(req.getReader());
        JsonApiDocument jsonApiDoc = JsonApiDocument.parse(rawJson);
        logger.debug("POST: " + URL + "  " + jsonApiDoc.toString(2));
        if (jsonApiDoc.hasData()) {
        	if (jsonApiDoc.isDataArray()) {
        		logger.error("Not yet impl save jsonApiDoc dataArray: " +jsonApiDoc.toString(2) );
                resp.sendError(400, "Not yet impl save jsonApiDoc dataArray: " );
        	}
        	JsonApiResource apiObj = jsonApiDoc.getData();
            if (apiObj.getType().equals(jsonType)) {
            	logger.debug("POST: " + URL + "  type: " + apiObj.getType()+ "  id: " + apiObj.optId());
                // security, limit to simple filename
                Matcher m = filenamePattern.matcher(apiObj.getId());
                if (!m.matches()) {
                    resp.sendError(400, "Bad id: " + apiObj.getId());
                    return;
                }
                updateBeforeSave(apiObj);
                save(apiObj);

                PrintWriter w = resp.getWriter();
                w.print(jsonApiDoc.toString(2));
                w.close();
                logger.debug("POST: " + URL + "  Done");
            } else {
                resp.sendError(400, "type  wrong/missing: " + apiObj.getType()+" != "+jsonType);
                return;
            }
        } else {
        	logger.warn("Unable to parse JSON: "+rawJson);
            resp.sendError(400, "Unable to parse JSON");
            return;
        }
        } catch(JSONException e) {
        	logger.warn("JSONException with: "+rawJson, e);
            throw new ServletException(e);
        } catch(RuntimeException e) {
            logger.error("doPost ", e);
            throw e;
        } catch (JsonApiException e) {
        	logger.warn("JsonApiException with: "+rawJson+" ", e);
            throw new ServletException(e);
		}
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPut(req, resp);
    }

    // @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebAdmin.setJsonHeader(req, resp);
        String URL = req.getRequestURL().toString();
        System.out.println("JsonToFileServlet doPatch " + URL);
        logger.debug("JsonToFileServlet doPatch " + URL);
        Matcher matcher = idPattern.matcher(URL);
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        try {
            if (matcher.matches()) {
                String id = matcher.group(1);

				JsonApiResource res = load(id);
                JsonApiDocument inJson = JsonApiDocument.parse(JsonApi.loadFromReader(req.getReader()));
                logger.info("patch: "+inJson.toString(2));
                if (inJson.isDataArray()) {
                    JsonApi.encodeError(out, "can't PATCH data array: " + URL);
                    writer.close();
                    resp.sendError(500);
                    return;
                }
                for (Iterator<String> iter = inJson.getData().attributeKeys(); iter.hasNext(); ) {
                    String key = iter.next();
                	res.setAttribute(key, inJson.getData().getAttribute(key));
                }
                for (Iterator<String> iter = inJson.getData().relationshipKeys(); iter.hasNext(); ) {
                    String key = iter.next();
                    JsonApiDocument rel = inJson.getData().getRelationship(key);
                    logger.warn("doPatch Rel "+key+"  "+rel);
                    if (rel == null) {
                    	logger.info("doPatch rel is null");
                    	res.setRelationshipNull(key);
                    } else if (rel.isDataArray()) {
                    	res.deleteRelationship(key);
                    	for (JsonApiResource relRes: rel.getDataArray()) {
                        	String relId = relRes.getId();
                        	String relType = relRes.getType();
                        	res.appendRelationship(key, relId, relType);
                    	}
                    } else if (rel.hasData()) {
                    	String relId = rel.getData().getId();
                    	String relType = rel.getData().getType();
                    	res.setRelationship(key, relId, relType);
                    } else {
                    	throw new RuntimeException("Shouldn't happen but did...");
                    }
                }
                logger.info("doPath after Rel: "+res.toString(2));
                updateBeforeSave(res);
                logger.info("doPath after updateBeforeSave: "+res.toString(2));
                save(res);
                writer.print(JsonApiDocument.createForResource(res).toString(2));
            } else {
                logger.warn("Bad URL for servlet: " + URL);
                JsonApi.encodeError(out, "bad url for servlet: " + URL);
                writer.close();
                resp.sendError(500);
            }
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(JsonApiException e) {
            throw new ServletException(e);
        } finally {
            writer.close();
            logger.debug("PATCH: " + URL + "  Done");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
        System.out.println("DELETE: " + URL);
        logger.info("DELETE: " + URL);
        Matcher matcher = idPattern.matcher(URL);
        if (matcher.matches()) {
            String pId = matcher.group(1);
            File f = new File(jsonDir, pId);
            f.delete();
        }
        resp.setStatus(resp.SC_NO_CONTENT);
        resp.getWriter().close();// empty content

        logger.info("DELETE: " + URL+" Done");
        } catch(JSONException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            if (req.getMethod().equals("PATCH")) {
                logger.debug("JsonToFileServlet.service PATCH");
                doPatch(req, resp);
            } else {
                super.service(req, resp);
            }
        } catch(ServletException e) {
            logger.error("problem ", e);
        }
    }

    protected List<String> getAllJsonIds() {
        return Arrays.asList(jsonDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                Matcher matcher = filenamePattern.matcher(name);
                return matcher.matches();
            }
        }));
    }

    protected void save(JsonApiResource inJson) throws IOException {
        BufferedWriter out = null;
        try {
            File pFile = new File(jsonDir, inJson.getId());
            logger.debug("Save to "+pFile.getAbsolutePath());
            if (pFile.getAbsolutePath().contains("quakeStationMeasurements")) {
                logger.debug("json: "+inJson.toString(2));
            }
            if (pFile.exists()) {
                Files.move(pFile.toPath(),
                           new File(jsonDir, inJson.getId() + ".old").toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
            }
            out = new BufferedWriter(new FileWriter(pFile));
            out.write(inJson.toString(2));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }


    protected void save(String id, JSONArray inJson) throws IOException {
        BufferedWriter out = null;
        try {
            File pFile = new File(jsonDir, id);
            logger.debug("Save to "+pFile.getAbsolutePath());
            if (pFile.exists()) {
                Files.move(pFile.toPath(),
                           new File(jsonDir, id + ".old").toPath(),
                           StandardCopyOption.REPLACE_EXISTING);
            }
            out = new BufferedWriter(new FileWriter(pFile));
            out.write(inJson.toString(2));
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    protected boolean exists(String id) {

        // security, limit to simple filename
        Matcher m = filenamePattern.matcher(id);
        if (m.matches()) {
        	File f = new File(jsonDir, id);
        	return f.exists();
        } else {
        	throw new RuntimeException("json id does not match pattern: " + id);
        }
    }

    protected JsonApiResource load(String id) throws IOException, JSONException, JsonApiException {
        // security, limit to simple filename
        Matcher m = filenamePattern.matcher(id);
        if (m.matches()) {
            BufferedReader in = null;
            try {
                File f = new File(jsonDir, id);
                if (f.exists()) {
                    logger.debug("Load from "+f.getAbsolutePath());
                    in = new BufferedReader(new FileReader(f));
                    return JsonApiResource.parse(JsonApi.loadFromReader(in));
                } else {
                    logger.debug("no file, createEmpty");
                    return createEmpty(id);
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch(IOException e) {
                        // oh well
                    }
                }
            }
        } else {
            throw new RuntimeException("json id does not match pattern: " + id);
        }
    }

    protected JSONArray loadArray(String id) throws IOException, JSONException, JsonApiException {
        // security, limit to simple filename
        Matcher m = filenamePattern.matcher(id);
        if (m.matches()) {
            BufferedReader in = null;
            try {
                File f = new File(jsonDir, id);
                if (f.exists()) {
                    logger.debug("Load from "+f.getAbsolutePath());
                    in = new BufferedReader(new FileReader(f));
                    JSONArray out = new JSONArray(JsonApi.loadFromReader(in));
                    return out;
                } else {
                    logger.debug("no file, createEmpty");
                    return new JSONArray();
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch(IOException e) {
                        // oh well
                    }
                }
            }
        } else {
            throw new RuntimeException("json id does not match pattern: " + id);
        }
    }

    protected JsonApiResource createEmpty(final String id) throws FileNotFoundException {
        // this is dumb...
        throw new FileNotFoundException("No data for id: "+id);
    }

    protected List<JsonApiResource> createEmptyArray(String id) {
        return new ArrayList<JsonApiResource>();
    }
    
    protected void updateBeforeSave(JsonApiResource jsonApiResource) throws IOException, JsonApiException {
        
    }
    
    protected void updateAfterLoad(JsonApiDocument jsonApiDocument) throws IOException, JsonApiException {
        
    }

    
    File getDirectory(JSONObject in) {
        return jsonDir;
    }
    
    String jsonType;

    Pattern allPattern;

    Pattern idPattern;

    Pattern filenamePattern = Pattern.compile("[-_a-zA-Z0-9]+");

    private String baseUrl;

    private File baseDir; 
    
    private File jsonDir;
    
    boolean isArrayType;

    private static final JSONObject EMPTY_JSON = new JSONObject();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JsonToFileServlet.class);
}
