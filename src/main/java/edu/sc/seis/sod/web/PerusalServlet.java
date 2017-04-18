package edu.sc.seis.sod.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
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

import org.hibernate.Query;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.PerusalJson;

public class PerusalServlet extends HttpServlet {

    public PerusalServlet() {
        this(WebAdmin.getApiBaseUrl());
    }

    public PerusalServlet(String baseUrl) {
        this.baseUrl = baseUrl;
        this.perusalDir = new File("perusals");
        if (!perusalDir.exists()) {
            perusalDir.mkdirs();
            logger.info("Create perusal Dir: " + perusalDir.getAbsolutePath());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            logger.info("GET: " + URL);
            if (req.getHeader("accept") != null && req.getHeader("accept").contains("application/vnd.api+json")) {
                resp.setContentType("application/vnd.api+json");
                logger.info("      contentType: application/vnd.api+json");
            } else {
                resp.setContentType("application/json");
                logger.info("      contentType: application/json");
            }
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            Matcher matcher = allPattern.matcher(URL);
            if (matcher.matches()) {
                // all perusal ids
                List<String> perusalIds = getPerusalIds();
                List<JsonApiData> jsonList = new ArrayList<JsonApiData>();
                for (String p : perusalIds) {
                    jsonList.add(new PerusalJson(p, baseUrl));
                }
                JsonApi.encodeJson(out, jsonList);
            } else {
                matcher = idPattern.matcher(URL);
                if (matcher.matches()) {
                    String pId = matcher.group(1);
                    JSONObject p = loadPerusal(pId);
                    updateNext(p);
                    writer.print(p);
                } else {
                    logger.warn("Bad URL for servlet: " + URL);
                    JsonApi.encodeError(out, "bad url for servlet: " + URL);
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
        JSONObject inJson = loadFromReader(req.getReader());
        System.out.println("POST: " + inJson.toString(2));
        JSONObject dataObj = inJson.getJSONObject("data");
        if (dataObj != null) {
            String type = dataObj.getString("type");
            String id = dataObj.optString("id");
            if (type.equals("perusal")) {
                if (id.length() == 0) {
                    // empty id, so new, create
                    id = java.util.UUID.randomUUID().toString();
                    dataObj.put("id", id);
                }
                // security, limit to simple filename
                Matcher m = filenamePattern.matcher(id);
                if (!m.matches()) {
                    resp.sendError(400, "Bad id: " + id);
                    return;
                }
                BufferedWriter out = null;
                try {
                    out = new BufferedWriter(new FileWriter(new File(perusalDir, id)));
                    out.write(inJson.toString(2));
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }
                updateNext(inJson);
                PrintWriter w = resp.getWriter();
                w.print(inJson.toString(2));
                w.close();
            } else {
                resp.sendError(400, "type  wrong/missing: " + type);
                return;
            }
        } else {
            resp.sendError(400, "Unable to parse JSON");
            return;
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPut(req, resp);
    }

    // @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("doPatch");
        // temp...
        doPut(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        logger.info("DELETE: " + URL);
        Matcher matcher = idPattern.matcher(URL);
        if (matcher.matches()) {
            String pId = matcher.group(1);
            File f = new File(perusalDir, pId);
            f.delete();
        }
        resp.getWriter().close();//empty content
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH"))
            doPatch(req, resp);
        else
            super.service(req, resp);
    }

    protected List<String> getPerusalIds() {
        return Arrays.asList(perusalDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                Matcher matcher = filenamePattern.matcher(name);
                return matcher.matches();
            }
        }));
    }

    protected JSONObject loadPerusal(String id) throws IOException {
        // security, limit to simple filename
        Matcher m = filenamePattern.matcher(id);
        if (m.matches()) {
            BufferedReader in = null;
            try {
                File f = new File(perusalDir, id);
                in = new BufferedReader(new FileReader(f));
                return loadFromReader(in);
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
            throw new RuntimeException("perusal id does not match pattern: " + id);
        }
    }

    protected JSONObject loadFromReader(BufferedReader in) throws IOException {
        StringBuffer json = new StringBuffer();
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = in.read(buf)) != -1) {
            json.append(String.valueOf(buf, 0, numRead));
        }
        JSONObject out = new JSONObject(json.toString());
        return out;
    }

    protected void updateNext(JSONObject p) {
        JSONObject related = p.getJSONObject("data").getJSONObject("relationships");
        JSONObject attr = p.getJSONObject("data").getJSONObject("attributes");
        JSONObject curr = related.optJSONObject("currESP");
        JSONObject prev = related.optJSONObject("prevESP");
        if (curr == null || curr.getJSONObject("data").getString("id").length() == 0) {
            // no next, so find
            int prevDbId = 0;
            EventStationPair next = null;
            if (prev == null || prev.getJSONObject("data").getString("id").length() == 0) {
                // no prev, so first time
                if (attr.getString(KEY_PRIMARY_SORT).equalsIgnoreCase(KEY_SORT_BY_EVENT)) {
                    next = getNextPrimaryEvent(null);
                    // no more left
                } else {
                    next = getNextPrimaryStation(null);
                    // no more left
                }
            } else {
                prevDbId = Integer.parseInt(prev.getJSONObject("data").getString("id"));
                String q = "from EventStationPair where dbid = " + prevDbId;
                Query query = SodDB.getSession().createQuery(q);
                EventStationPair esp = (EventStationPair)query.uniqueResult();
                List<EventStationPair> nextESPList;
                StatefulEvent currEvent = esp.getEvent();
                StationImpl currStation = esp.getStation();
                if (attr.getString(KEY_PRIMARY_SORT).equalsIgnoreCase(KEY_SORT_BY_EVENT)) {
                    nextESPList = SodDB.getSingleton().getSuccessfulESPForStation(currStation);
                } else {
                    nextESPList = SodDB.getSingleton().getSuccessfulESPForEvent(currEvent);
                }
                Iterator<EventStationPair> iterator = nextESPList.iterator();
                while (iterator.hasNext()) {
                    EventStationPair eventStationPair = iterator.next();
                    if (eventStationPair.getDbid() == prevDbId) {
                        // found it
                        break;
                    }
                }
                if (iterator.hasNext()) {
                    next = iterator.next();
                } else {
                    // no next in sub-sort, go to next in primary
                    if (attr.getString(KEY_PRIMARY_SORT).equalsIgnoreCase(KEY_SORT_BY_EVENT)) {
                        next = getNextPrimaryEvent(currEvent);
                        // no more left
                    } else {
                        next = getNextPrimaryStation(currStation);
                        // no more left
                    }
                }
            }
            if (next != null) {
                EventStationJson esJson = new EventStationJson(next, baseUrl);
                JSONObject nextJSON = new JSONObject();
                JSONObject dataJSON = new JSONObject();
                dataJSON.put("type", esJson.getType());
                dataJSON.put("id", esJson.getId());
                nextJSON.put("data", dataJSON);
                related.put("currESP", nextJSON);
            }
        }
    }

    private EventStationPair getNextPrimaryStation(StationImpl currStation) {
        List<StationImpl> allStationList = Arrays.asList(NetworkDB.getSingleton().getAllStations());
        // should sort here
        Iterator<StationImpl> staIt = allStationList.iterator();
        while (currStation != null && staIt.hasNext()) {
            StationImpl s = staIt.next();
            if (s.getDbid() == currStation.getDbid()) {
                // found it
                break;
            }
        }
        while (staIt.hasNext()) {
            StationImpl s = staIt.next();
            List<EventStationPair> nextESPList = SodDB.getSingleton().getSuccessfulESPForStation(s);
            if (nextESPList.size() > 0) {
                return nextESPList.get(0);
            }
        }
        return null;
    }

    private EventStationPair getNextPrimaryEvent(StatefulEvent currEvent) {
        SuccessfulEventCache cache = WebAdmin.getSuccessfulEventCache();
        List<StatefulEvent> events = cache.getEventWithSuccessful();
        // should sort here...
        Iterator<StatefulEvent> eventIt = events.iterator();
        while (currEvent != null && eventIt.hasNext()) {
            StatefulEvent e = eventIt.next();
            if (e.getDbid() == currEvent.getDbid()) {
                break;
            }
        }
        while (eventIt.hasNext()) {
            StatefulEvent e = eventIt.next();
            List<EventStationPair> nextESPList = SodDB.getSingleton().getSuccessfulESPForEvent(e);
            if (nextESPList.size() > 0) {
                return nextESPList.get(0);
            }
        }
        return null;
    }

    Pattern allPattern = Pattern.compile(".*/perusals");

    Pattern idPattern = Pattern.compile(".*/perusals/([-_a-zA-Z0-9]+)");

    Pattern filenamePattern = Pattern.compile("[-_a-zA-Z0-9]+");

    private String baseUrl;

    private File perusalDir;

    public static final String KEY_PRIMARY_SORT = "primary-sort";

    public static final String KEY_STATION_SORT = "station-sort";

    public static final String KEY_EVENT_SORT = "event-sort";

    public static final String KEY_SORT_BY_EVENT = "event";

    public static final String KEY_SORT_BY_STATION = "station";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PerusalServlet.class);
}
