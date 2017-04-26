package edu.sc.seis.sod.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

import org.hibernate.Query;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.PerusalJson;

public class PerusalServlet extends HttpServlet {

    private static final String PREV_ESP = "prev";

    public static final String CURR_ESP = "curr";

    public static final String NEXT_ESP = "next";

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
            System.out.println("GET: " + URL);
            logger.info("GET: " + URL);
            WebAdmin.setJsonHeader(req, resp);
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
                JsonApi.encodeJsonWithoutInclude(out, jsonList);
            } else {
                matcher = idPattern.matcher(URL);
                if (matcher.matches()) {
                    String pId = matcher.group(1);
                    JSONObject p = loadPerusal(pId);
                    writer.print(p.toString(2));
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
        WebAdmin.setJsonHeader(req, resp);
        String URL = req.getRequestURL().toString();
        JSONObject inJson = loadFromReader(req.getReader());
        System.out.println("POST: " + URL + "  " + inJson.toString(2));
        JSONObject dataObj = inJson.getJSONObject(JsonApi.DATA);
        if (dataObj != null) {
            String type = dataObj.getString(JsonApi.TYPE);
            String id = dataObj.optString(JsonApi.ID);
            if (type.equals(PerusalJson.PERUSAL)) {
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
                updateNext(inJson);
                savePerusal(id, inJson);
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
        WebAdmin.setJsonHeader(req, resp);
        String URL = req.getRequestURL().toString();
        System.out.println("doPatch " + URL);
        Matcher matcher = idPattern.matcher(URL);
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        try {
            if (matcher.matches()) {
                String id = matcher.group(1);
                JSONObject p = loadPerusal(id);
                JSONObject inJson = loadFromReader(req.getReader());
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
                updateNext(p);
                savePerusal(id, p);
                writer.print(p.toString(2));
            } else {
                logger.warn("Bad URL for servlet: " + URL);
                JsonApi.encodeError(out, "bad url for servlet: " + URL);
                writer.close();
                resp.sendError(500);
            }
        } finally {
            writer.close();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("DELETE: " + URL);
        logger.info("DELETE: " + URL);
        Matcher matcher = idPattern.matcher(URL);
        if (matcher.matches()) {
            String pId = matcher.group(1);
            File f = new File(perusalDir, pId);
            f.delete();
        }
        resp.setStatus(resp.SC_NO_CONTENT);
        resp.getWriter().close();// empty content
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

    private void savePerusal(String id, JSONObject inJson) throws IOException {
        BufferedWriter out = null;
        try {
            File pFile = new File(perusalDir, id);
            if (pFile.exists()) {
                Files.move(pFile.toPath(),
                           new File(perusalDir, id + ".old").toPath(),
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

    protected int extractESP_dbid(JSONObject curr) {
        if (curr == null) {
            return -1;
        }
        if (!curr.has(JsonApi.DATA)) {
            return -1;
        }
        JSONObject data = curr.optJSONObject(JsonApi.DATA);
        if (data == null) {
            return -1;
        }
        if (!data.has(JsonApi.ID)) {
            return -1;
        }
        return Integer.parseInt(data.optString(JsonApi.ID, "-4"));
    }

    protected void updateNext(JSONObject p) {
        System.out.println("updateNext: " + p.toString(2));
        JSONObject related = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
        JSONObject attr = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.ATTRIBUTES);
        JSONObject curr = related.optJSONObject(CURR_ESP);
        JSONObject prev = related.optJSONObject(PREV_ESP);
        JSONObject next = related.optJSONObject(NEXT_ESP);
        System.err.println("curr data: " + curr.optJSONObject(JsonApi.DATA));
        int currDbId = extractESP_dbid(curr);
        int prevDbId = extractESP_dbid(prev);
        int nextDbId = extractESP_dbid(next);
        String primarySort = attr.optString(KEY_PRIMARY_SORT, KEY_SORT_BY_QUAKE);
        String quakeSort = attr.optString(KEY_PRIMARY_SORT, KEY_SORT_BY_TIME);
        String stationSort = attr.optString(KEY_PRIMARY_SORT, KEY_SORT_BY_DISTANCE);
        if (currDbId < 0) {
            // no next, so find
            if (prevDbId > 0) {
                // have prev, so set curr and next based on prev
                EventStationPair currESP = findNext(prevDbId, primarySort, quakeSort, stationSort);
                if (currESP != null) {
                    EventStationJson esJson = new EventStationJson(currESP, baseUrl);
                    related.put(CURR_ESP, formJsonRelatedFromESP(esJson));
                    EventStationPair nextESP = findNext(currESP.getDbid(), primarySort, quakeSort, stationSort);
                    esJson = new EventStationJson(nextESP, baseUrl);
                    related.put(NEXT_ESP, formJsonRelatedFromESP(esJson));
                }
            } else if (nextDbId > 0) {
                // have next but not prev, curr, so back out curr and prev from
                // next
            } else {
                // nothing, so start at beginning
                EventStationPair currESP = findNext(-1, primarySort, quakeSort, stationSort);
                if (currESP != null) {
                    EventStationJson esJson = new EventStationJson(currESP, baseUrl);
                    related.put(CURR_ESP, formJsonRelatedFromESP(esJson));
                    EventStationPair nextESP = findNext(currESP.getDbid(), primarySort, quakeSort, stationSort);
                    esJson = new EventStationJson(nextESP, baseUrl);
                    related.put(NEXT_ESP, formJsonRelatedFromESP(esJson));
                }
            }
        } else {
            // have curr, so find prev and next based off curr
            EventStationPair nextESP = findNext(currDbId, primarySort, quakeSort, stationSort);
            if (nextESP != null) {
                EventStationJson esJson = new EventStationJson(nextESP, baseUrl);
                related.put(NEXT_ESP, formJsonRelatedFromESP(esJson));
            }
        }
    }

    private JSONObject formJsonRelatedFromESP(EventStationJson esJson) {
        JSONObject currJSON = new JSONObject();
        JSONObject dataJSON = new JSONObject();
        dataJSON.put(JsonApi.TYPE, esJson.getType());
        dataJSON.put(JsonApi.ID, esJson.getId());
        currJSON.put(JsonApi.DATA, dataJSON);
        return currJSON;
    }

    private EventStationPair findNext(long prevDbId, String primarySort, String quakeSort, String stationSort) {
        EventStationPair next = null;
        if (prevDbId <= 0) {
            // no prev, so first time
            if (primarySort.equalsIgnoreCase(KEY_SORT_BY_QUAKE)) {
                next = getNextPrimaryEvent(null);
                // no more left
            } else {
                next = getNextPrimaryStation(null);
                // no more left
            }
        } else {
            String q = "from EventStationPair where dbid = " + prevDbId;
            Query query = SodDB.getSession().createQuery(q);
            EventStationPair esp = (EventStationPair)query.uniqueResult();
            List<EventStationPair> nextESPList;
            StatefulEvent currEvent = esp.getEvent();
            StationImpl currStation = esp.getStation();
            if (primarySort.equalsIgnoreCase(KEY_SORT_BY_QUAKE)) {
                // same quake, look for next station
                nextESPList = SodDB.getSingleton().getSuccessfulESPForEvent(currEvent);
            } else {
                // same stations, look for next quake
                nextESPList = SodDB.getSingleton().getSuccessfulESPForStation(currStation);
            }
            Iterator<EventStationPair> iterator = nextESPList.iterator();
            while (iterator.hasNext()) {
                EventStationPair eventStationPair = iterator.next();
                if (eventStationPair.getDbid() == prevDbId) {
                    // found it
                    break;
                }
            }
            while (iterator.hasNext()) {
                next = iterator.next();
                if (SodDB.getSingleton().getNumSuccessful(next.getEvent(), next.getStation()) > 0) {
                    return next;
                }
            }
            // no next in sub-sort, go to next in primary
            if (primarySort.equalsIgnoreCase(KEY_SORT_BY_QUAKE)) {
                next = getNextPrimaryEvent(currEvent);
                // no more left
            } else {
                next = getNextPrimaryStation(currStation);
                // no more left
            }
        }
        return next;
    }

    private EventStationPair getNextPrimaryStation(StationImpl currStation) {
        List<StationImpl> allStationList = Arrays.asList(NetworkDB.getSingleton().getAllStations());
        // should sort here
        Iterator<StationImpl> staIt = allStationList.iterator();
        StationImpl currS = null;
        while (currStation != null && staIt.hasNext()) {
            currS = staIt.next();
            if (currS.getDbid() == currStation.getDbid()) {
                // found it
                break;
            }
        }
        while (staIt.hasNext()) {
            StationImpl s = staIt.next();
            List<EventStationPair> nextESPList = SodDB.getSingleton().getSuccessfulESPForStation(s);
            Iterator<EventStationPair> espIt = nextESPList.iterator();
            while (espIt.hasNext()) {
                EventStationPair nEsp = espIt.next();
                if (SodDB.getSingleton().getNumSuccessful(nEsp.getEvent(), nEsp.getStation()) > 0) {
                    return nEsp;
                }
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
            Iterator<EventStationPair> espIt = nextESPList.iterator();
            while (espIt.hasNext()) {
                EventStationPair nEsp = espIt.next();
                if (SodDB.getSingleton().getNumSuccessful(nEsp.getEvent(), nEsp.getStation()) > 0) {
                    return nEsp;
                }
            }
        }
        return null;
    }

    Pattern allPattern = Pattern.compile(".*/perusals");

    Pattern idPattern = Pattern.compile(".*/perusals/([-_a-zA-Z0-9]+)");

    Pattern filenamePattern = Pattern.compile("[-_a-zA-Z0-9]+");

    private String baseUrl;

    private File perusalDir;

    private static final JSONObject EMPTY_JSON = new JSONObject();

    public static final String KEY_PRIMARY_SORT = "primary-sort";

    public static final String KEY_STATION_SORT = "station-sort";

    public static final String KEY_EVENT_SORT = "quake-sort";

    public static final String KEY_SORT_BY_QUAKE = "quake";

    public static final String KEY_SORT_BY_STATION = "station";

    public static final String KEY_SORT_BY_TIME = "time";

    public static final String KEY_SORT_BY_DISTANCE = "distance";

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PerusalServlet.class);
}
