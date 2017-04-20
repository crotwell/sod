package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.sod.AbstractEventChannelPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.status.eventArm.EventMonitor;
import edu.sc.seis.sod.web.jsonapi.EventJson;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;

public class EventServlet extends HttpServlet {

    public EventServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        logger.info("GET: " + URL);
        Matcher matcher = singleEvent.matcher(URL);
        WebAdmin.setJsonHeader(req, resp);
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        if (matcher.matches()) {
            String dbid = matcher.group(1);
            try {
                StatefulEvent e = StatefulEventDB.getSingleton().getEvent(Integer.parseInt(dbid));
                JsonApi.encodeJson(out, new EventJson(e, WebAdmin.getApiBaseUrl()));
            } catch(NumberFormatException e) {
                throw new RuntimeException(e);
            } catch(NotFound e) {
                throw new RuntimeException(e);
            }
        } else {
            matcher = eventStations.matcher(URL);
            if (matcher.matches()) {
                int dbid = Integer.parseInt(matcher.group(1));
                StatefulEvent event;
                try {
                    event = StatefulEventDB.getSingleton().getEvent(dbid);
                    
                    // want only successful ESP that actually have successful ECP, otherwise even-station may pass 
                    // but no even-channels or waveforms pass
                    List<AbstractEventChannelPair> ecpList = SodDB.getSingleton().getSuccessful(event);
                    List<EventStationPair> espList = new ArrayList<EventStationPair>();
                    for (AbstractEventChannelPair ecp : ecpList) {
                        if ( ! espList.contains(ecp.getEsp())) {
                            espList.add(ecp.getEsp());
                        }
                    }
                    
                    List<JsonApiData> jsonData = new ArrayList<JsonApiData>(espList.size());
                    for (EventStationPair esp : espList) {
                        jsonData.add(new EventStationJson(esp, WebAdmin.getApiBaseUrl()));
                    }
                    JsonApi.encodeJson(out, jsonData);
                } catch(NotFound e) {
                    throw new RuntimeException(e);
                }
            } else {
                matcher = allEvents.matcher(URL);
                if (matcher.matches()) {
                // logger.debug("doGet all");
                try {
                    SuccessfulEventCache cache = WebAdmin.getSuccessfulEventCache();
                    List<StatefulEvent> events = cache.getEventWithSuccessful();
                    List<JsonApiData> eventJsonList = new ArrayList<JsonApiData>();
                    for (StatefulEvent statefulEvent : events) {
                        EventJson eventJson = new EventJson(statefulEvent, WebAdmin.getApiBaseUrl());
                        eventJson.setNumSuccessfulStations(cache.getNumSuccessful(statefulEvent));
                        eventJsonList.add(eventJson);
                    }
                    JsonApi.encodeJson(out, eventJsonList);
                } catch(JSONException e) {
                    throw new ServletException(e);
                }
                } else {
                    logger.warn("bad url for servlet: regex=" + allEvents +" or "+singleEvent+" or "+eventStations);
                    JsonApi.encodeError(out, "bad url for servlet: regex=" + allEvents +" or "+singleEvent+" or "+eventStations);
                    writer.close();
                    resp.sendError(500);
                }
            }
        }
        writer.close();
    }
    
    Pattern allEvents = Pattern.compile(".*/quakes");

    Pattern singleEvent = Pattern.compile(".*/quakes/([0-9]+)");

    Pattern eventStations = Pattern.compile(".*/quakes/([0-9]+)/stations");
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventServlet.class);
}
