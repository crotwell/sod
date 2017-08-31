package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.seisFile.fdsnws.stationxml.BaseNodeType;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.bag.TauPUtil;
import edu.sc.seis.sod.hibernate.EventDB;
import edu.sc.seis.sod.hibernate.NetworkDB;
import edu.sc.seis.sod.hibernate.NotFound;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;
import edu.sc.seis.sod.web.jsonapi.TauPJson;


public class TauPServlet  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            logger.info("GET: " + URL);
            WebAdmin.setJsonHeader(req, resp);
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            Map<String, String[]> params = req.getParameterMap();
            TauPUtil taup;
            if (params.containsKey(MODEL)) {
                taup = TauPUtil.getTauPUtil(params.get(MODEL)[0]);
            } else {
                taup = TauPUtil.getTauPUtil();
            }
            double depth = 0;
            if (params.containsKey(EVDEPTH)) {
                depth = Double.parseDouble(params.get(EVDEPTH)[0]);
            }
            String[] phases = DEFAULT_PHASES;
            if (params.containsKey(PHASES)) {
                phases = params.get(PHASES)[0].split(",");
            }
            if (params.containsKey(DISTDEG)) {
                double distDeg = Double.parseDouble(params.get(DISTDEG)[0]);
                List<Arrival> arrivalList = taup.calcTravelTimes(distDeg, depth, phases);
                encodeArrivalsList(out, arrivalList);
                
            } else if (params.containsKey(STATION) && params.containsKey(EVENT)) {
                Station sta = null;
                Matcher staMatcher = netStaCodePattern.matcher(params.get(STATION)[0]);
                if (staMatcher.matches()) {
                    String netCode = staMatcher.group(1);
                    String year = staMatcher.group(3);
                    String staCode = staMatcher.group(4);
                    List<Station> staList = NetworkDB.getSingleton().getStationByCodes(netCode, staCode);
                    if (year == null && ! NetworkIdUtil.isTemporary(netCode)) {
                        // perm net code, should only be one
                        sta = staList.get(0);
                    } else if (year == null) {
                        JsonApi.encodeError(out, "temp netcode requires code_year: " + URL);
                        writer.close();
                        resp.sendError(500);
                    } else {
                        Instant netBegin = BaseNodeType.parseISOString(year+"1231T23:59:59.000Z");
                        for (Station stationImpl : staList) {
                            TimeRange staTR = new TimeRange(stationImpl.getEffectiveTime());
                            if (staTR.contains(netBegin)) {
                                sta = stationImpl;
                            }
                        }
                    }
                    if (sta == null) {
                        JsonApi.encodeError(out, "station not found: " + URL);
                        writer.close();
                        resp.sendError(500);
                    }
                } else {
                    int stationDbid = Integer.parseInt(params.get(STATION)[0]);
                    sta = NetworkDB.getSingleton().getStation(stationDbid);   
                }
                int eventDbid = Integer.parseInt(params.get(EVENT)[0]);
                CacheEvent evt = EventDB.getSingleton().getEvent(eventDbid);
                List<Arrival> arrivalList = taup.calcTravelTimes(sta, evt.getPreferred(), phases);

                encodeArrivalsList(out, arrivalList);
            } else {
                logger.warn("Bad URL for servlet: "+URL);
                JsonApi.encodeError(out, "bad url for servlet: " + URL);
                writer.close();
                resp.sendError(500);
            }
            writer.close();
        } catch(TauModelException e) {
            throw new ServletException(e);
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(NumberFormatException e) {
            throw new ServletException(e);
        } catch(NotFound e) {
            throw new ServletException(e);
        } catch(NoPreferredOrigin e) {
            throw new ServletException(e);
        } finally {
            NetworkDB.rollback();
        }
    }
    
    private void encodeArrivalsList(JSONWriter out, List<Arrival> arrivalList) {
        List<JsonApiData> jsonList = new ArrayList<JsonApiData>();
        for (Arrival arrival : arrivalList) {
            jsonList.add(new TauPJson(arrival, WebAdmin.getApiBaseUrl()));
        }
        JsonApi.encodeJson(out, jsonList);
    }

    Pattern netStaCodePattern = Pattern.compile(NetworkServlet.networkIdStationCodeStr);
    
    public static final String MODEL = "model";
    public static final String EVDEPTH = "evdepth";
    public static final String PHASES = "phases";
    public static final String[] DEFAULT_PHASES = {"p", "s", "P", "S"};
    public static final String DISTDEG = "distdeg";
    public static final String STATION = "station";
    public static final String EVENT = "event";
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TauPServlet.class);
}
