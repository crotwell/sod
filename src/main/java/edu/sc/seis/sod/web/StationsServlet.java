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

import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.hibernate.NotFound;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.eventpair.AbstractEventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventStationPair;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.web.jsonapi.ChannelJson;
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
        logger.info("GET: " + URL);
        WebAdmin.setJsonHeader(req, resp);
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        NetworkDB netdb = NetworkDB.getSingleton();
        Matcher matcher = stationPattern.matcher(URL);
        if (matcher.matches()) {
            // logger.debug("station");
            String netCode = matcher.group(1);
            String year = matcher.group(3);
            String staCode = matcher.group(4);
            List<StationImpl> staList = netdb.getStationByCodes(netCode, staCode);
            if (staList.size() > 0) {
            StationImpl sta = staList.get(0);
            JsonApi.encodeJson(out, new StationJson(sta, WebAdmin.getApiBaseUrl()));
            resp.setStatus(HttpServletResponse.SC_OK);
            writer.close();
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writer.println("No station with code "+netCode+"."+staCode+" found");
                writer.close();
            }
        } else {
            matcher = stationDbidPattern.matcher(URL);
            if (matcher.matches()) {
                String dbid = matcher.group(1);
                StationImpl sta = null;
                try {
                    sta = netdb.getStation(Integer.parseInt(dbid));
                    List<ChannelImpl> chans = netdb.getChannelsForStation(sta);
                    if (sta != null) {
                        JsonApi.encodeJson(out, new StationJson(sta, chans, WebAdmin.getApiBaseUrl()));
                        
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        JsonApi.encodeError(out, "Station is null for dbid " + dbid);
                    }
                } catch(NumberFormatException e) {
                    JsonApi.encodeError(out, "NumberFormatException " + e.getMessage());
                } catch(NotFound e) {
                    JsonApi.encodeError(out, "NotFound " + e.getMessage());
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

                    // want only successful ESP that actually have successful ECP, otherwise even-station may pass 
                    // but no even-channels or waveforms pass
                    List<AbstractEventChannelPair> ecpList = SodDB.getSingleton().getSuccessful(sta);
                    List<EventStationPair> eventList = new ArrayList<EventStationPair>();
                    for (AbstractEventChannelPair ecp : ecpList) {
                        if ( ! eventList.contains(ecp.getEsp())) {
                            eventList.add(ecp.getEsp());
                        }
                    }
                    List<JsonApiData> jsonData = new ArrayList<JsonApiData>(eventList.size());
                    for (EventStationPair esp : eventList) {
                        jsonData.add(new EventStationJson(esp, WebAdmin.getApiBaseUrl()));
                    }
                    JsonApi.encodeJson(out, jsonData);
                    writer.close();
                    resp.setStatus(HttpServletResponse.SC_OK);
                    

                } else {
                    matcher = stationChannelsPattern.matcher(URL);
                    if (matcher.matches()) {
                        // logger.debug("station");
                        String netCode = matcher.group(1);
                        String year = matcher.group(3);
                        String staCode = matcher.group(4);
                        StationImpl sta = netdb.getStationByCodes(netCode, staCode).get(0);
                        List<ChannelImpl> chans = netdb.getChannelsForStation(sta);
                        List<JsonApiData> jsonData = new ArrayList<JsonApiData>(chans.size());
                        for (ChannelImpl channelImpl : chans) {
                            jsonData.add(new ChannelJson(channelImpl, WebAdmin.getApiBaseUrl()));
                        }
                        JsonApi.encodeJson(out, jsonData);
                        writer.close();
                        resp.setStatus(HttpServletResponse.SC_OK);
                    
                    } else {
                        logger.warn("bad url for servlet: regex=" + stationPattern.toString());
                        JsonApi.encodeError(out, "bad url for servlet: regex=" + stationPattern.toString());
                        writer.close();
                        resp.sendError(500);
                    }
                }
            }
        }
        NetworkDB.rollback();
    }

    Pattern stationDbidPattern = Pattern.compile(".*/stations/([0-9]+)");

    Pattern stationPattern = Pattern.compile(".*" + NetworkServlet.stationIdPatternStr);

    Pattern stationEventsPattern = Pattern.compile(".*" + NetworkServlet.stationIdPatternStr + "/quakes");

    Pattern stationChannelsPattern = Pattern.compile(".*" + NetworkServlet.stationIdPatternStr + "/channels");
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StationsServlet.class);
}
