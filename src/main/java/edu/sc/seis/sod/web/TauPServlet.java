package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
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

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.EventDB;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.TauPJson;


public class TauPServlet  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String URL = req.getRequestURL().toString();
            logger.info("GET: " + URL);
            if (req.getHeader("accept") != null && req.getHeader("accept").contains("application/vnd.api+json")) {
                resp.setContentType("application/vnd.api+json");
                logger.info("      contentType: application/vnd.api+json");
            } else {
                resp.setContentType("application/json");
                logger.info("      contentType: application/json");
            }
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
                phases = params.get(PHASES);
            }
            if (params.containsKey(DISTDEG)) {
                double distDeg = Double.parseDouble(params.get(DISTDEG)[0]);
                List<Arrival> arrivalList = taup.calcTravelTimes(distDeg, depth, phases);
                TauPJson json = new TauPJson(WebAdmin.getBaseUrl());
                json.encode(arrivalList, out);
            } else if (params.containsKey(STATION) && params.containsKey(EVENT)) {
                StationImpl sta = null;
                Matcher staMatcher = netStaCodePattern.matcher(params.get(STATION)[0]);
                if (staMatcher.matches()) {
                    String netCode = staMatcher.group(1);
                    String year = staMatcher.group(3);
                    String staCode = staMatcher.group(4);
                    List<StationImpl> staList = NetworkDB.getSingleton().getStationByCodes(netCode, staCode);
                    if (year == null && ! NetworkIdUtil.isTemporary(netCode)) {
                        // perm net code, should only be one
                        sta = staList.get(0);
                    } else if (year == null) {
                        JsonApi.encodeError(out, "temp netcode requires code_year: " + URL);
                        writer.close();
                        resp.sendError(500);
                    } else {
                        MicroSecondDate netBegin = new MicroSecondDate(year+"1231T23:59:59.000Z");
                        for (StationImpl stationImpl : staList) {
                            MicroSecondTimeRange staTR = new MicroSecondTimeRange(stationImpl.getEffectiveTime());
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
                TauPJson json = new TauPJson(WebAdmin.getBaseUrl());
                json.encode(arrivalList, out);
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
