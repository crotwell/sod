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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

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

                out.object();
                out.key(JsonApi.DATA).array();
                for (String p : jsonlIds) {
                    out.object();
                    out.key(JsonApi.ID).value(p);
                    out.key(JsonApi.TYPE).value(jsonType);
                    out.endObject();
                }
                out.endArray();
                out.key(JsonApi.INCLUDED).array();
                String comma = "";
                for (String pId : jsonlIds) {
                    JSONObject p = load(pId).getJSONObject(JsonApi.DATA);
                    writer.println(comma);
                    writer.println(p.toString(2));
                    comma = ",";
                }
                out.endArray();
                out.endObject();
               
            } else {
                matcher = idPattern.matcher(URL);
                if (matcher.matches()) {
                    String pId = matcher.group(1);
                    JSONObject p = load(pId);
                    updateAfterLoad(p);
                    writer.print(p.toString(2));
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
        try {
        WebAdmin.setJsonHeader(req, resp);
        String URL = req.getRequestURL().toString();
        JSONObject inJson = JsonApi.loadFromReader(req.getReader());
        System.out.println("POST: " + URL + "  " + inJson.toString(2));
        logger.debug("POST: " + URL + "  " + inJson.toString(2));
        JSONObject dataObj = inJson.getJSONObject(JsonApi.DATA);
        if (dataObj != null) {
            String type = dataObj.getString(JsonApi.TYPE);
            String id = dataObj.optString(JsonApi.ID);//missing id means newly created
            if (type.equals(jsonType)) {
                if (id.length() == 0) {
                    // empty id, so new, create
                    id = java.util.UUID.randomUUID().toString();
                    dataObj.put(JsonApi.ID, id);
                }
                // security, limit to simple filename
                Matcher m = filenamePattern.matcher(id);
                if (!m.matches()) {
                    resp.sendError(400, "Bad id: " + id);
                    return;
                }
                updateBeforeSave(inJson);
                save(id, inJson);
                PrintWriter w = resp.getWriter();
                w.print(inJson.toString(2));
                w.close();
                logger.debug("POST: " + URL + "  Done");
            } else {
                resp.sendError(400, "type  wrong/missing: " + type+" != "+jsonType);
                return;
            }
        } else {
            resp.sendError(400, "Unable to parse JSON");
            return;
        }
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(RuntimeException e) {
            logger.error("doPost ", e);
            throw e;
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
                JSONObject p = load(id);
                JSONObject inJson = JsonApi.loadFromReader(req.getReader());
                JSONObject pRelated = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
                JSONObject pAttr = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.ATTRIBUTES);
                JSONObject inRelated = inJson.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
                JSONObject inAttr = inJson.getJSONObject(JsonApi.DATA).optJSONObject(JsonApi.ATTRIBUTES);
                if (inAttr != null) {
                    if (pAttr == null) {
                        pAttr = new JSONObject();
                        p.getJSONObject(JsonApi.DATA).put(JsonApi.ATTRIBUTES, pAttr);
                    }
                    Iterator<String> keyIt = inAttr.keys();
                    while (keyIt.hasNext()) {
                        String key = keyIt.next();
                        pAttr.put(key, inAttr.get(key));
                    }
                }
                if (inRelated != null) {
                    if (pRelated == null) {
                        pRelated = new JSONObject();
                        p.getJSONObject(JsonApi.DATA).put(JsonApi.RELATIONSHIPS, pRelated);
                    }
                    Iterator<String> keyIt = inRelated.keys();
                    while (keyIt.hasNext()) {
                        String key = keyIt.next();
                        pRelated.put(key, inRelated.get(key));
                    }
                }
                updateBeforeSave(p);
                save(id, p);
                writer.print(p.toString(2));
            } else {
                logger.warn("Bad URL for servlet: " + URL);
                JsonApi.encodeError(out, "bad url for servlet: " + URL);
                writer.close();
                resp.sendError(500);
            }
        } catch(JSONException e) {
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

    protected void save(String id, JSONObject inJson) throws IOException {
        BufferedWriter out = null;
        try {
            File pFile = new File(jsonDir, id);
            logger.debug("Save to "+pFile.getAbsolutePath());
            if (pFile.getAbsolutePath().contains("quakeStationMeasurements")) {
                logger.debug("json: "+inJson.toString(2));
            }
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

    protected JSONObject load(String id) throws IOException {
        // security, limit to simple filename
        Matcher m = filenamePattern.matcher(id);
        if (m.matches()) {
            BufferedReader in = null;
            try {
                File f = new File(jsonDir, id);
                if (f.exists()) {
                    logger.debug("Load from "+f.getAbsolutePath());
                    in = new BufferedReader(new FileReader(f));
                    return JsonApi.loadFromReader(in);
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

    protected JSONObject createEmpty(final String id) throws FileNotFoundException {
        // this is dumb...
        throw new FileNotFoundException("No data for id: "+id);
    }
    
    protected void updateBeforeSave(JSONObject p) throws IOException {
        
    }
    
    protected void updateAfterLoad(JSONObject p) throws IOException {
        
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

    private static final JSONObject EMPTY_JSON = new JSONObject();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JsonToFileServlet.class);
}
