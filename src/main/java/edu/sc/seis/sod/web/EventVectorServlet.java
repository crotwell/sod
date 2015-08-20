package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
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
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.web.jsonapi.EventStationJson;
import edu.sc.seis.sod.web.jsonapi.EventVectorJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class EventVectorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        resp.setContentType("application/vnd.api+json");
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        Matcher m = eventStationPattern.matcher(URL);
        if (m.matches()) {
            Query q = AbstractHibernateDB.getReadOnlySession().createQuery("from " + SodDB.getSingleton().getEcpClass().getName()
                    + " where dbid = " + m.group(1));
            AbstractEventChannelPair esp = (AbstractEventChannelPair)q.uniqueResult();
            EventVectorJson jsonData = new EventVectorJson(esp, WebAdmin.getBaseUrl());
            JsonApi.encodeJson(out, jsonData);
        } else {
            JsonApi.encodeError(out, "url does not match " + eventStationPattern.pattern());
        }
    }

    Pattern eventStationPattern = Pattern.compile(".*/eventvectors/([0-9]+)");
}