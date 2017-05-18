package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.hibernate.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class PerusalServlet  extends JsonToFileServlet {

    public PerusalServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "perusals");
        // TODO Auto-generated constructor stub
    }

    private static final String FIRST_ESP = "first";

    private static final String PREV_ESP = "prev";

    public static final String CURR_ESP = "curr";

    public static final String NEXT_ESP = "next";

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

    @Override
    protected void updateBeforeSave(JSONObject p) throws IOException {
        try {
        updateNext(p);
        updateMeasurementPerusalLink(p);
        } catch (JSONException e) {
            System.err.println(e);
            System.err.println(p.toString(2));
            logger.error("Error in updateBeforeSave: ", e);
            throw e;
        }
    }
    
    @Override
    protected void updateAfterLoad(JSONObject p) throws IOException {
        JSONObject related = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
        JSONObject curr = related.optJSONObject(CURR_ESP);
        int currDbId = extractESP_dbid(curr);
        if (currDbId < 0 ) {
            // need curr, so updateNext
            updateNext(p);
        }
    }

    protected void updateMeasurementPerusalLink(JSONObject p) throws IOException {
        String pId = p.getJSONObject(JsonApi.DATA).getString(JsonApi.ID);
        JSONObject related = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
        if (related.has("tools")) {
            JSONArray tools = related.getJSONObject("tools").getJSONArray(JsonApi.DATA);

            MeasurementToolServlet mtServlet = new MeasurementToolServlet();
            for( int i=0; i<tools.length(); i++) {
                JSONObject mt = tools.getJSONObject(i);
                logger.debug("Try to update perusal on "+mt.toString(2));
                String mId = mt.getString(JsonApi.ID);
                logger.debug("Got mId: "+mId);
                JSONObject mObj = mtServlet.load(mId);
                logger.debug("Load MT "+mObj.toString(2));
                JSONObject mObjPerusal = mObj.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS).getJSONObject("perusal").getJSONObject(JsonApi.DATA);
                logger.debug("Got mObjPerusal");
                if( mObjPerusal.isNull(JsonApi.ID) || ! pId.equals(mObjPerusal.getString(JsonApi.ID))) {
                    mObjPerusal.put(JsonApi.ID, pId);
                    mtServlet.save(mId, mObj);
                }
            }
        }
    }

    protected void updateNext(JSONObject p) {
        System.out.println("updateNext: " + p.toString(2));
        JSONObject related = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.RELATIONSHIPS);
        JSONObject attr = p.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.ATTRIBUTES);
        JSONObject first = related.optJSONObject(FIRST_ESP);
        JSONObject curr = related.optJSONObject(CURR_ESP);
        JSONObject prev = related.optJSONObject(PREV_ESP);
        JSONObject next = related.optJSONObject(NEXT_ESP);
        System.err.println("curr data: " + curr.optJSONObject(JsonApi.DATA));
        int firstDbId = extractESP_dbid(first);
        int currDbId = extractESP_dbid(curr);
        int prevDbId = extractESP_dbid(prev);
        int nextDbId = extractESP_dbid(next);
        String primarySort = attr.optString(KEY_PRIMARY_SORT, KEY_SORT_BY_QUAKE);
        String quakeSort = attr.optString(KEY_PRIMARY_SORT, KEY_SORT_BY_TIME);
        String stationSort = attr.optString(KEY_PRIMARY_SORT, KEY_SORT_BY_DISTANCE);
        if (firstDbId>0 && currDbId < 0 && prevDbId < 0 && nextDbId < 0) {
            // likely going back to beginning, set prevDbId to firstDbId so if below will pick up
            // at right place
            prevDbId = firstDbId;
        }
        if (currDbId < 0) {
            // no curr, so find
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
                // have next but not prev, curr, so move next to curr and leave prev null
                // this might not be right in case of client trying to do "double-previous" but...
                curr = next;
                currDbId = nextDbId;
                related.put(CURR_ESP, curr);
                EventStationPair nextESP = findNext(currDbId, primarySort, quakeSort, stationSort);
                EventStationJson esJson = new EventStationJson(nextESP, baseUrl);
                related.put(NEXT_ESP, formJsonRelatedFromESP(esJson));
                
            } else {
                // nothing, so start at beginning
                EventStationPair currESP = findNext(-1, primarySort, quakeSort, stationSort);
                if (currESP != null) {
                    EventStationJson esJson = new EventStationJson(currESP, baseUrl);
                    related.put(CURR_ESP, formJsonRelatedFromESP(esJson));
                    related.put(FIRST_ESP, formJsonRelatedFromESP(esJson)); // also set first
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
        
        System.out.println("Done updateNext: " + p.toString(2));
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
