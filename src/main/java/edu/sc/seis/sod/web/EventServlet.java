package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.EventDB;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;
import edu.sc.seis.sod.web.jsonapi.EventJson;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.NetworkJson;

public class EventServlet extends HttpServlet {

    public EventServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        Matcher matcher = singleEvent.matcher(URL);
        if (req.getHeader("accept") != null && req.getHeader("accept").contains("application/vnd.api+json")) {
            resp.setContentType("application/vnd.api+json");
            System.out.println("      contentType: application/vnd.api+json");
        } else {
            resp.setContentType("application/json");
            System.out.println("      contentType: application/json");
        }
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        if (matcher.matches()) {
            String dbid = matcher.group(1);
            try {
                StatefulEvent e = StatefulEventDB.getSingleton().getEvent(Integer.parseInt(dbid));
                JsonApi.encodeJson(out, new EventJson(e, WebAdmin.getBaseUrl()));
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
                    List<EventStationPair> espList = SodDB.getSingleton().getSuccessfulESPForEvent(event);
                    List<JsonApiData> jsonData = new ArrayList<JsonApiData>(espList.size());
                    for (EventStationPair esp : espList) {
                        jsonData.add(new EventStationJson(esp, WebAdmin.getBaseUrl()));
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
                    List<StatefulEvent> events = StatefulEventDB.getSingleton().getAll();
                    List<JsonApiData> eventJsonList = new ArrayList<JsonApiData>();
                    for (StatefulEvent statefulEvent : events) {
                        eventJsonList.add(new EventJson(statefulEvent, WebAdmin.getBaseUrl()));
                    }
                    JsonApi.encodeJson(out, eventJsonList);
                } catch(JSONException e) {
                    throw new ServletException(e);
                }
                } else {
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
}
