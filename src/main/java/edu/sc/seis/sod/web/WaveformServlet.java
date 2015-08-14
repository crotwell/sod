package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.EventDB;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class WaveformServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        resp.setContentType("application/vnd.api+json");
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        try {
            NetworkDB netdb = NetworkDB.getSingleton();
            Matcher staMatcher = stationPattern.matcher(URL);
            Matcher eventMatcher = eventPattern.matcher(URL);
            if (staMatcher.find() && eventMatcher.find()) {

                System.out.println("GET: " + URL+"  in matcher.find");
                String netCode = staMatcher.group(1);
                String year = staMatcher.group(3);
                NetworkAttrImpl n = NetworkServlet.loadNet(netCode, year);
                System.out.println("GET: " + URL+"  net "+n.get_code());

                String staCode = staMatcher.group(7);
                List<StationImpl> staList = NetworkDB.getSingleton().getStationForNet(n, staCode);
                System.out.println("GET: " + URL+"  sta "+staCode+" "+staList.size());
                CacheEvent e = EventDB.getSingleton().getEvent(Integer.parseInt(eventMatcher.group(1)));
                System.out.println("GET: " + URL+"  event");
                MicroSecondDate originTime = e.getPreferred().getTime();
                StationImpl s = null;
                for (StationImpl stationImpl : staList) {
                    MicroSecondTimeRange staRange = new MicroSecondTimeRange(stationImpl.getEffectiveTime());
                    if (staRange.contains(originTime)) {
                        s = stationImpl;
                        break;
                    }
                }
                if (s != null) {
                    System.out.println("GET: " + URL+"  s overlap e");
                    EventStationPair esp = SodDB.getSingleton().getEventStationPair(e, s);
                    System.out.println("GET: " + URL+"  esp");
                    if (esp != null) {
                    // fix for more than one station
                    EventStationJson esJson = new EventStationJson(esp, WebAdmin.getBaseUrl());
                    JsonApi.encodeJson(out, esJson);
                    } else {
                        System.out.println("GET: " + URL+"  esp nnot found");
                        JsonApi.encodeError(out, "EventStation Pair not found");
                        resp.sendError(404);
                    }
                } else {
                    System.out.println("GET: " + URL+"  station not found out of "+staList.size());
                    JsonApi.encodeError(out, "Station not found");
                    writer.close();
                    resp.sendError(404);
                }
            }
            writer.close();
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(NumberFormatException e) {
            throw new ServletException(e);
        } catch(NotFound e) {
            JsonApi.encodeError(out, "Event not found");
            resp.sendError(500);
        } catch(NoPreferredOrigin e) {
            throw new ServletException(e);
        }
    }

    Pattern stationPattern = Pattern.compile(NetworkServlet.networkIdPatternStr + NetworkServlet.stationIdPatternStr);

    Pattern eventPattern = Pattern.compile("/events/([0-9]+)");
}
