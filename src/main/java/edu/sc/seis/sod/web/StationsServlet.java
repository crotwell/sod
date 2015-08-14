package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.web.jsonapi.EventJson;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.StationJson;

public class StationsServlet extends HttpServlet {

    public StationsServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        resp.setContentType("application/vnd.api+json");
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        NetworkDB netdb = NetworkDB.getSingleton();
        Matcher matcher = stationPattern.matcher(URL);
        if (matcher.matches()) {
            // logger.debug("station");
            String netCode = matcher.group(1);
            String year = matcher.group(3);
            String staCode = matcher.group(4);
            StationImpl sta = netdb.getStationByCodes(netCode, staCode).get(0);
            JsonApi.encodeJson(out, new StationJson(sta, WebAdmin.getBaseUrl()));
            resp.setStatus(HttpServletResponse.SC_OK);
            writer.close();
        } else {
            matcher = stationDbidPattern.matcher(URL);
            if (matcher.matches()) {
                String dbid = matcher.group(1);
                StationImpl sta = null;
                try {
                    sta = netdb.getStation(Integer.parseInt(dbid));
                } catch(NumberFormatException e) {
                    JsonApi.encodeError(out, "NumberFormatException "+e.getMessage());
                } catch(NotFound e) {
                    JsonApi.encodeError(out, "NotFound "+e.getMessage());
                }
                if (sta != null) {
                JsonApi.encodeJson(out, new StationJson(sta, WebAdmin.getBaseUrl()));
                resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    JsonApi.encodeError(out, "Station is null for dbid "+dbid);
                }
                writer.close();
            } else {
                matcher = stationEventsPattern.matcher(URL);
                if (matcher.matches()) {
                    // logger.debug("station");
                    String netCode = matcher.group(1);
                    String year = matcher.group(3);
                    String staCode = matcher.group(4);
                    StationImpl sta = netdb.getStationByCodes(netCode, staCode).get(0);
                    List<EventStationPair> eventList = SodDB.getSingleton().getSuccessfulESPForStation(sta);
                    List<JsonApiData> jsonData = new ArrayList<JsonApiData>(eventList.size());
                    for (EventStationPair esp : eventList) {
                        jsonData.add(new EventStationJson(esp, WebAdmin.getBaseUrl()));
                    }
                    JsonApi.encodeJson(out, jsonData);
                    writer.close();
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    JsonApi.encodeError(out, "bad url for servlet: regex=" + stationPattern.toString());
                    writer.close();
                    resp.sendError(500);
                }
            }
        }
        NetworkDB.rollback();
    }

    Pattern stationDbidPattern = Pattern.compile(".*/stations/([0-9]+)");

    Pattern stationPattern = Pattern.compile(".*" + NetworkServlet.stationIdPatternStr);

    Pattern stationEventsPattern = Pattern.compile(".*" + NetworkServlet.stationIdPatternStr + "/events");
}
