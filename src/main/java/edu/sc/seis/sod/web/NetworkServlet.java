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

import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.NetworkJson;
import edu.sc.seis.sod.web.jsonapi.StationJson;

public class NetworkServlet extends HttpServlet {

    public NetworkServlet() {
        this(WebAdmin.getBaseUrl());
    }

    public NetworkServlet(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            System.out.println("GET: " + URL);
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            NetworkDB netdb = NetworkDB.getSingleton();
            Matcher matcher = allNetworkPattern.matcher(URL);
            if (matcher.matches()) {
                // all nets
                List<NetworkAttrImpl> netList = netdb.getAllNetworks();
                List<JsonApiData> jsonList = new ArrayList<JsonApiData>();
                for (NetworkAttrImpl net : netList) {
                    jsonList.add(new NetworkJson(net, baseUrl));
                }
                JsonApi.encodeJson(out, jsonList);
            } else {
                matcher = networkPattern.matcher(URL);
                if (matcher.matches()) {
                    String netCode = matcher.group(1);
                    String year = matcher.group(3);
                    
                    List<NetworkAttrImpl> netList = netdb.getNetworkByCode(netCode);
                    NetworkAttrImpl n = null;
                    if (NetworkIdUtil.isTemporary(netCode) || year == null) {
                        // might not be right if temp net but year not given...?
                     n = netList.get(0); 
                    } else {
                        for (NetworkAttrImpl netImpl : netList) {
                            if (NetworkIdUtil.getYear(netImpl.get_id()).equals(year)) {
                                n = netImpl;
                                break;
                            }
                        }
                    }
                    JsonApi.encodeJson(out, new NetworkJson(n, baseUrl));
                } else {
                    matcher = stationListPattern.matcher(URL);
                    if (matcher.matches()) {
                        // logger.debug("stationList");
                        String netCode = matcher.group(1);
                        List<StationImpl> staList = netdb.getStationForNet(netdb.getNetworkByCode(netCode).get(0));
                        JsonApi.encodeJson(out, StationJson.toJsonList(staList, baseUrl));
                    } else {
                        matcher = stationRelationshipPattern.matcher(URL);
                        if (matcher.matches()) {
                         // logger.debug("stationList");
                            String netCode = matcher.group(1);
                            List<StationImpl> staList = netdb.getStationForNet(netdb.getNetworkByCode(netCode).get(0));
                            JsonApi.encodeJson(out, StationJson.toJsonList(staList, baseUrl));
                        } else {
                            matcher = stationPattern.matcher(URL);
                            if (matcher.matches()) {
                                // logger.debug("station");
                                String netCode = matcher.group(1);
                                String staCode = matcher.group(2);
                                StationImpl sta = netdb.getStationByCodes(netCode, staCode).get(0);
                                JsonApi.encodeJson(out, new StationJson(sta, baseUrl));
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
        }
    }

    String baseUrl;

    Pattern allNetworkPattern = Pattern.compile(".*/networks");

    String networkPatternStr = ".*/networks/([A-Z0-9]+)(_([0-9])+)?";
    Pattern networkPattern = Pattern.compile(networkPatternStr);

    Pattern stationListPattern = Pattern.compile(networkPatternStr+"/stations");

    Pattern stationRelationshipPattern = Pattern.compile(networkPatternStr+"/relationships/stations");

    Pattern stationPattern = Pattern.compile(networkPatternStr+"/stations/([A-Z0-9]+)");
    Pattern stationPattern2 = Pattern.compile(".*/networks/([A-Z0-9]+).([A-Z0-9]+)");

    Pattern channelPattern = Pattern.compile(".*/networks/([A-Z0-9]+).([A-Z0-9]+).([A-Z0-9][A-Z0-9]).([A-Z0-9][A-Z0-9][A-Z0-9])");
}
