package edu.sc.seis.sod.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Query;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;

import edu.sc.seis.fissuresUtil.hibernate.AbstractHibernateDB;
import edu.sc.seis.sod.AbstractEventChannelPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.EventVectorJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;

public class EventStationServlet extends HttpServlet {

    public EventStationServlet() {
        
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equals("PATCH"))
            doPatch(req, resp);
        else
            super.service(req, resp);
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        logger.info("GET: " + URL);
        WebAdmin.setJsonHeader(req, resp);
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        Matcher m = eventStationPattern.matcher(URL);
        if (m.matches()) {
            Query q = AbstractHibernateDB.getSession().createQuery("from " + EventStationPair.class.getName()
                    + " where dbid = " + m.group(1));
            EventStationPair esp = (EventStationPair)q.uniqueResult();
            if (esp == null) {
                JsonApi.encodeError(out, "esp not found " + m.group(1));
                writer.close();
                resp.sendError(500);
            }
            q = AbstractHibernateDB.getSession().createQuery("from " + SodDB.getSingleton().getEcpClass().getName()
                    + " where esp = " + esp.getDbid() + "  and status.stageInt = " + Stage.PROCESSOR.getVal()
                    + " and status.standingInt = " + Standing.SUCCESS.getVal());
            List<AbstractEventChannelPair> ecpList = new ArrayList<AbstractEventChannelPair>();
            List tmp = q.list();
            for (Object obj : tmp) {
                if (obj == null) {
                    throw new RuntimeException("obj from hibernate is null");
                }
                ecpList.add((AbstractEventChannelPair)obj);
            }
            EventStationJson jsonData = new EventStationJson(esp, ecpList, WebAdmin.getApiBaseUrl());
            JsonApi.encodeJson(out, jsonData);
        } else {
            m = eventVectorPattern.matcher(URL);
            if (m.matches()) {
                Query q = AbstractHibernateDB.getSession().createQuery("from " + SodDB.getSingleton().getEcpClass().getName()
                        + " where esp = " + m.group(1) + "  and status.stageInt = " + Stage.PROCESSOR.getVal()
                        + " and status.standingInt = " + Standing.SUCCESS.getVal());
                List<JsonApiData> jsonData = new ArrayList<JsonApiData>();
                List tmp = q.list();
                for (Object obj : tmp) {
                    if (obj == null) {
                        throw new RuntimeException("obj from hibernate is null");
                    }
                    jsonData.add(new EventVectorJson((AbstractEventChannelPair)obj, WebAdmin.getApiBaseUrl()));
                }
                JsonApi.encodeJson(out, jsonData);
            } else {
                m = measurementsPattern.matcher(URL);
                if (m.matches()) {
                    measurementServlet.doGet(req, resp);
                } else {
                    logger.warn("url does not match " + eventStationPattern.pattern());
                    JsonApi.encodeError(out, "url does not match " + eventStationPattern.pattern());
                    writer.close();
                    resp.sendError(500);
                }
            }
        }
        writer.close();
        AbstractHibernateDB.rollback();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("doPost " + URL);
        Matcher matcher = measurementsPattern.matcher(URL);
        if (matcher.matches()) {
            measurementServlet.doPost(req, resp);
        } else {
            logger.warn("url does not match " + measurementsPattern.pattern());
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            JsonApi.encodeError(out, "EventStationServlet: url does not match " + measurementsPattern.pattern());
            writer.close();
            resp.sendError(500);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("doPut " + URL);
        Matcher matcher = measurementsPattern.matcher(URL);
        if (matcher.matches()) {
            measurementServlet.doPut(req, resp);
        } else {
            logger.warn("url does not match " + measurementsPattern.pattern());
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            JsonApi.encodeError(out, "EventStationServlet: url does not match " + measurementsPattern.pattern());
            writer.close();
            resp.sendError(500);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("doDelete " + URL);
        Matcher matcher = measurementsPattern.matcher(URL);
        if (matcher.matches()) {
            measurementServlet.doDelete(req, resp);
        } else {
            logger.warn("url does not match " + measurementsPattern.pattern());
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            JsonApi.encodeError(out, "EventStationServlet: url does not match " + measurementsPattern.pattern());
            writer.close();
            resp.sendError(500);
        }
    }

    // @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebAdmin.setJsonHeader(req, resp);
        String URL = req.getRequestURL().toString();
        System.out.println("doPatch " + URL);
        Matcher matcher = eventStationPattern.matcher(URL);
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        try {
            if (matcher.matches()) {
                String id = matcher.group(1);
                Query q = AbstractHibernateDB.getSession().createQuery("from " + EventStationPair.class.getName()
                                                                       + " where dbid = " + matcher.group(1));
                EventStationPair esp = (EventStationPair)q.uniqueResult();
                if (esp == null) {
                    JsonApi.encodeError(out, "esp not found " + matcher.group(1));
                    writer.close();
                    resp.sendError(500);
                }

                JSONObject inJson = JsonApi.loadFromReader(req.getReader());
                logger.debug("doPatch: "+inJson.toString(2));
                Map<String, Serializable> cookies = esp.getCookies();
                JSONObject measurements = inJson.getJSONObject(JsonApi.DATA).getJSONObject(JsonApi.ATTRIBUTES).getJSONObject("measurements");
                Iterator<String> keyIt = measurements.keys();
                while(keyIt.hasNext()) {
                    String key = keyIt.next();
                    logger.debug("Measurement "+key+": "+measurements.get(key).toString());
                }
            } else {
                matcher = measurementsPattern.matcher(URL);
                if (matcher.matches()) {
                    measurementServlet.doGet(req, resp);
                } else {
                    logger.warn("url does not match " + eventStationPattern.pattern());
                    JsonApi.encodeError(out, "EventStationServlet: url does not match " + eventStationPattern.pattern());
                    writer.close();
                    resp.sendError(500);
                }
            }
        } finally {
            writer.close();
        }
    }
    
    Pattern eventStationPattern = Pattern.compile(".*/quake-stations/([0-9]+)");

    Pattern eventVectorPattern = Pattern.compile(".*/quake-stations/([0-9]+)/quake-vectors");
    
    Pattern measurementsPattern = Pattern.compile(".*/quake-stations/([0-9]+)/measurements");
    
    QuakeStationMeasurementsServlet measurementServlet = new QuakeStationMeasurementsServlet();
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventStationServlet.class);
}