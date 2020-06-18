package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import org.hibernate.query.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiDocument;
import edu.sc.seis.sod.web.jsonapi.JsonApiException;
import edu.sc.seis.sod.web.jsonapi.JsonApiResource;

public class PerusalServlet  extends JsonToFileServlet {

    public PerusalServlet() {
        super(WebAdmin.getApiBaseUrl(), new File("jsonData"), "perusals");
        // TODO Auto-generated constructor stub
    }

    private static final String FIRST_ESP = "first";

    private static final String PREV_ESP = "prev";

    public static final String CURR_ESP = "curr";

    public static final String NEXT_ESP = "next";

    protected int extractESP_dbid(JsonApiDocument jsonApiDocument) throws JsonApiException {
        if (jsonApiDocument == null) {
            return -1;
        }
        if (!jsonApiDocument.hasData()) {
            return -1;
        }
        JsonApiResource data = jsonApiDocument.getData();
        if (data == null) {
            return -1;
        }
        if (data.optId() == null) {
            return -1;
        }
        return Integer.parseInt(data.getId());
    }

    @Override
    protected void updateBeforeSave(JsonApiResource p) throws IOException, JsonApiException {
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
    protected void updateAfterLoad(JsonApiDocument p) throws IOException, JsonApiException {
    	JsonApiResource res = p.getData();
    	JsonApiDocument curr = res.getRelationship(CURR_ESP);
        
        int currDbId = extractESP_dbid(curr);
        if (currDbId < 0 ) {
            // need curr, so updateNext
            updateNext(res);
        }
    }

    protected void updateMeasurementPerusalLink(JsonApiResource p) throws IOException, JSONException, JsonApiException {

        logger.info("updateMeasurementPerusalLink "+p.toString(2));
        String pId = p.getId();
        if (p.hasRelationship("tools")) {
            JsonApiDocument tools = p.getRelationship("tools");

            MeasurementToolServlet mtServlet = new MeasurementToolServlet();
            for (JsonApiResource mt : tools.getDataArray()) {
                logger.debug("Try to update perusal on "+mt.toString(2));
                String mId = mt.getId();
                logger.debug("Got mId: "+mId);
                JsonApiResource mObj = mtServlet.load(mId);
                logger.debug("Load MT "+mObj.toString(2));
                
                mObj.setRelationship("perusal", pId, "perusal");
                mtServlet.save(mObj);
            }
        }
    }

    protected void updateNext(JsonApiResource res) throws JsonApiException {
        System.out.println("updateNext: " + res.toString(2));
        
        
        JsonApiDocument first = res.getRelationship(FIRST_ESP);
        JsonApiDocument curr = res.getRelationship(CURR_ESP);
        JsonApiDocument prev = res.getRelationship(PREV_ESP);
        JsonApiDocument next = res.getRelationship(NEXT_ESP);
        System.err.println("curr data: " + (curr == null ? "null" : curr.getData()));
        int firstDbId = extractESP_dbid(first);
        int currDbId = extractESP_dbid(curr);
        int prevDbId = extractESP_dbid(prev);
        int nextDbId = extractESP_dbid(next);
        String primarySort = res.getAttributeString(KEY_PRIMARY_SORT, KEY_SORT_BY_QUAKE);
        String quakeSort = res.getAttributeString(KEY_EVENT_SORT, KEY_SORT_BY_TIME);
        String stationSort = res.getAttributeString(KEY_STATION_SORT, KEY_SORT_BY_DISTANCE);
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
                    res.setRelationship(CURR_ESP, esJson.getId(), esJson.getType());
                    EventStationPair nextESP = findNext(currESP.getDbid(), primarySort, quakeSort, stationSort);
                    esJson = new EventStationJson(nextESP, baseUrl);
                    res.setRelationship(NEXT_ESP, esJson.getId(), esJson.getType());
                }
            } else if (nextDbId > 0) {
                // have next but not prev, curr, so move next to curr and leave prev null
                // this might not be right in case of client trying to do "double-previous" but...

            	EventStationPair currESP = findPrev(nextDbId, primarySort, quakeSort, stationSort);
            	if (currESP != null) {
                    EventStationJson esJson = new EventStationJson(currESP, baseUrl);
                    res.setRelationship(CURR_ESP, esJson.getId(), esJson.getType());
                    EventStationPair prevESP = findPrev(currESP.getDbid(), primarySort, quakeSort, stationSort);
                    esJson = new EventStationJson(prevESP, baseUrl);
                    res.setRelationship(PREV_ESP, esJson.getId(), esJson.getType());
            	}
                
            } else {
                // nothing, so start at beginning
                EventStationPair currESP = findNext(-1, primarySort, quakeSort, stationSort);
                if (currESP != null) {
                    EventStationJson esJson = new EventStationJson(currESP, baseUrl);
                    res.setRelationship(CURR_ESP, esJson.getId(), esJson.getType());
                    res.setRelationship(FIRST_ESP, esJson.getId(), esJson.getType()); // also set first
                    EventStationPair nextESP = findNext(currESP.getDbid(), primarySort, quakeSort, stationSort);
                    esJson = new EventStationJson(nextESP, baseUrl);
                    res.setRelationship(NEXT_ESP, esJson.getId(), esJson.getType());
                } else {
                	logger.warn("Perusal is empty, can't find first "+res.getId());
                }
            }
        } else {
            // have curr, so find prev and next based off curr
            EventStationPair nextESP = findNext(currDbId, primarySort, quakeSort, stationSort);
            if (nextESP != null) {
                EventStationJson esJson = new EventStationJson(nextESP, baseUrl);
                res.setRelationship(NEXT_ESP, esJson.getId(), esJson.getType());
            }
            EventStationPair prevESP = findPrev(currDbId, primarySort, quakeSort, stationSort);
            if (prevESP != null) {
                EventStationJson esJson = new EventStationJson(prevESP, baseUrl);
                res.setRelationship(PREV_ESP, esJson.getId(), esJson.getType());
            }
        }
        
        System.out.println("Done updateNext: " + res.toString(2));
    }

    private JSONObject formJsonRelatedFromESP(EventStationJson esJson) {
        JSONObject currJSON = new JSONObject();
        JSONObject dataJSON = new JSONObject();
        dataJSON.put(JsonApi.TYPE, esJson.getType());
        dataJSON.put(JsonApi.ID, esJson.getId());
        currJSON.put(JsonApi.DATA, dataJSON);
        return currJSON;
    }

    private EventStationPair findPrev(long currDbId, String primarySort, String quakeSort, String stationSort) {
        EventStationPair prev = null;
        if (currDbId <= 0) {
        	prev = null;
        } else {
            String q = "from EventStationPair where dbid = " + currDbId;
            Query query = SodDB.getSession().createQuery(q);
            EventStationPair esp = (EventStationPair)query.uniqueResult();
            List<EventStationPair> prevESPList;
            StatefulEvent currEvent = esp.getEvent();
            Station currStation = esp.getStation();
            if (primarySort.equalsIgnoreCase(KEY_SORT_BY_QUAKE)) {
                // same quake, look for prev station
                prevESPList = SodDB.getSingleton().getSuccessfulESPForEvent(currEvent);
            } else {
                // same stations, look for prev quake
                prevESPList = SodDB.getSingleton().getSuccessfulESPForStation(currStation);
            }
            ListIterator<EventStationPair> iterator = prevESPList.listIterator(prevESPList.size());
            EventStationPair eventStationPair = null;
            while (iterator.hasPrevious()) {
                eventStationPair = iterator.previous();
                if (eventStationPair.getDbid() == currDbId) {
                    // found it
                    break;
                }
            }
            while (iterator.hasPrevious()) {
            	prev = iterator.previous();
                if (SodDB.getSingleton().getNumSuccessful(prev.getEvent(), prev.getStation()) > 0) {
                    return prev;
                }
            }
            // no prev in sub-sort, go to prev in primary
            if (primarySort.equalsIgnoreCase(KEY_SORT_BY_QUAKE)) {
                prev = getPrevPrimaryEvent(currEvent, quakeSort);
                // no more left
            } else {
                prev = getPrevPrimaryStation(currStation, stationSort);
                // no more left
            }
        }
        return prev;
    }

    private EventStationPair getPrevPrimaryEvent(StatefulEvent currEvent, String quakeSort) {
        if (currEvent == null) {
        	return null;
        }
        SuccessfulEventCache cache = WebAdmin.getSuccessfulEventCache();
        List<StatefulEvent> events = cache.getEventWithSuccessful();
        // should sort here...
        // should sort here
        ListIterator<StatefulEvent> staIt = events.listIterator(events.size());

        // Iterate in reverse.
        while(staIt.hasPrevious()) {
        	StatefulEvent currS = null;
            currS = staIt.previous();
            if (currS.getDbid() == currEvent.getDbid()) {
                // found it
                break;
            }
        }
        while (staIt.hasPrevious()) {
        	StatefulEvent s = staIt.previous();
            List<EventStationPair> prevESPList = SodDB.getSingleton().getSuccessfulESPForEvent(s);
            ListIterator<EventStationPair> espIt = prevESPList.listIterator(prevESPList.size());
            while (espIt.hasPrevious()) {
                EventStationPair nEsp = espIt.previous();
                if (SodDB.getSingleton().getNumSuccessful(nEsp.getEvent(), nEsp.getStation()) > 0) {
                    return nEsp;
                }
            }
        }
        return null;
	}

    private EventStationPair getPrevPrimaryStation(Station currStation, String stationSort) {
        List<Station> allStationList = Arrays.asList(NetworkDB.getSingleton().getAllStations());
        if (currStation == null) {
        	return null;
        }
        // should sort here
        ListIterator<Station> staIt = allStationList.listIterator(allStationList.size());

        // Iterate in reverse.
        while(staIt.hasPrevious()) {
        	Station currS = null;
            currS = staIt.previous();
            if (currS.getDbid() == currStation.getDbid()) {
                // found it
                break;
            }
        }
        while (staIt.hasPrevious()) {
            Station s = staIt.previous();
            List<EventStationPair> prevESPList = SodDB.getSingleton().getSuccessfulESPForStation(s);
            ListIterator<EventStationPair> espIt = prevESPList.listIterator(prevESPList.size());
            while (espIt.hasPrevious()) {
                EventStationPair nEsp = espIt.previous();
                if (SodDB.getSingleton().getNumSuccessful(nEsp.getEvent(), nEsp.getStation()) > 0) {
                    return nEsp;
                }
            }
        }
        return null;
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
            Station currStation = esp.getStation();
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

    private EventStationPair getNextPrimaryStation(Station currStation) {
        List<Station> allStationList = Arrays.asList(NetworkDB.getSingleton().getAllStations());
        // should sort here
        Iterator<Station> staIt = allStationList.iterator();
        Station currS = null;
        while (currStation != null && staIt.hasNext()) {
            currS = staIt.next();
            if (currS.getDbid() == currStation.getDbid()) {
                // found it
                break;
            }
        }
        while (staIt.hasNext()) {
            Station s = staIt.next();
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
