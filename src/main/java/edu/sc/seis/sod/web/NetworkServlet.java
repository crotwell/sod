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

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.NetworkJson;
import edu.sc.seis.sod.web.jsonapi.StationJson;

public class NetworkServlet extends HttpServlet {

    public NetworkServlet() {
        this(WebAdmin.getApiBaseUrl());
    }

    public NetworkServlet(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            logger.info("GET: " + URL);
            WebAdmin.setJsonHeader(req, resp);
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            NetworkDB netdb = NetworkDB.getSingleton();
            Matcher matcher = allNetworkPattern.matcher(URL);
            if (matcher.matches()) {
                // all nets
                List<Network> netList = netdb.getAllNetworks();
                List<JsonApiData> jsonList = new ArrayList<JsonApiData>();
                for (Network net : netList) {
                    jsonList.add(new NetworkJson(net, baseUrl));
                }
                JsonApi.encodeJson(out, jsonList);
            } else {
                matcher = networkPattern.matcher(URL);
                if (matcher.matches()) {
                    String netCode = matcher.group(1);
                    String year = matcher.group(2);
                    Network n = loadNet(netCode, year);
                    JsonApi.encodeJson(out, new NetworkJson(n, baseUrl));
                } else {
                    matcher = stationListPattern.matcher(URL);
                    if (matcher.matches()) {
                         logger.debug("stationList "+matcher.group()+" net="+matcher.group(1)+" y="+matcher.group(2));
                        String netCode = matcher.group(1);
                        String year = matcher.group(2);
                        Network n = loadNet(netCode, year);
                        List<Station> staList = netdb.getStationForNet(n);
                        JsonApi.encodeJson(out, StationJson.toJsonList(staList, baseUrl));
                    } else {
                        matcher = stationRelationshipPattern.matcher(URL);
                        if (matcher.matches()) {
                         // logger.debug("stationList");
                            String netCode = matcher.group(1);
                            List<Station> staList = netdb.getStationForNet(netdb.getNetworkByCode(netCode).get(0));
                            JsonApi.encodeJson(out, StationJson.toJsonList(staList, baseUrl));
                        } else {
                            matcher = stationPattern.matcher(URL);
                            if (matcher.matches()) {
                                // logger.debug("station");
                                String netCode = matcher.group(1);
                                String year = matcher.group(2);
                                String staCode = matcher.group(3);
                                Station sta = netdb.getStationByCodes(netCode, staCode).get(0);
                                JsonApi.encodeJson(out, new StationJson(sta, baseUrl));
                            } else {
                                logger.warn("Bad URL for servlet: "+URL);
                                JsonApi.encodeError(out, "bad url for servlet: " + URL);
                                writer.close();
                                resp.sendError(500);
                            }
                        }
                    }
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
    
    public static Network loadNet(String netCode, String year) {
        List<Network> netList = NetworkDB.getSingleton().getNetworkByCode(netCode);
        Network n = null;
        if (NetworkIdUtil.isTemporary(netCode) || year == null) {
            // might not be right if temp net but year not given...?
         n = netList.get(0); 
        } else {
            for (Network netImpl : netList) {
                if (netImpl.getStartYearString().equals(year)) {
                    n = netImpl;
                    break;
                }
            }
        }
        return n;
    }

    String baseUrl;

    Pattern allNetworkPattern = Pattern.compile(".*/networks");

    public static String networkIdStr = "([A-Z0-9]{1,2})([0-9]{4})?";
    public static String networkIdPatternStr = "/networks/"+networkIdStr;
    public static String stationIdPatternStr = "/stations/"+networkIdStr+"\\.([A-Z0-9]+)";
    public String networkPatternStr = ".*"+networkIdPatternStr;
    
    Pattern networkPattern = Pattern.compile(networkPatternStr);

    Pattern stationListPattern = Pattern.compile(networkPatternStr+"/stations");

    Pattern stationRelationshipPattern = Pattern.compile(networkPatternStr+"/relationships/stations");

    public static String networkIdStationCodeStr = networkIdStr+".([A-Z0-9]+)";
    Pattern stationPattern = Pattern.compile(networkPatternStr+stationIdPatternStr);
    Pattern stationPattern2 = Pattern.compile(".*/networks/"+networkIdStationCodeStr);

    Pattern channelPattern = Pattern.compile(".*/networks/([A-Z0-9]+).([A-Z0-9]+).([A-Z0-9][A-Z0-9]).([A-Z0-9][A-Z0-9][A-Z0-9])");

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkServlet.class);
}
