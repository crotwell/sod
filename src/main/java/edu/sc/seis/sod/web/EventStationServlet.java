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

import org.hibernate.Query;
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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        if (req.getHeader("accept") != null && req.getHeader("accept").contains("application/vnd.api+json")) {
            resp.setContentType("application/vnd.api+json");
            System.out.println("      contentType: application/vnd.api+json");
        } else {
            resp.setContentType("application/json");
            System.out.println("      contentType: application/json");
        }
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        Matcher m = eventStationPattern.matcher(URL);
        if (m.matches()) {
            Query q = AbstractHibernateDB.getSession().createQuery("from " + EventStationPair.class.getName()
                    + " where dbid = " + m.group(1));
            EventStationPair esp = (EventStationPair)q.uniqueResult();
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
            EventStationJson jsonData = new EventStationJson(esp, ecpList, WebAdmin.getBaseUrl());
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
                    jsonData.add(new EventVectorJson((AbstractEventChannelPair)obj, WebAdmin.getBaseUrl()));
                }
                JsonApi.encodeJson(out, jsonData);
            } else {
                JsonApi.encodeError(out, "url does not match " + eventStationPattern.pattern());
            }
        }
        writer.close();
        AbstractHibernateDB.rollback();
    }

    Pattern eventStationPattern = Pattern.compile(".*/event-stations/([0-9]+)");

    Pattern eventVectorPattern = Pattern.compile(".*/event-stations/([0-9]+)/event-vectors");
}
