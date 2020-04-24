package edu.sc.seis.sod.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.query.Query;
import org.json.JSONWriter;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.hibernate.AbstractHibernateDB;
import edu.sc.seis.sod.hibernate.EventSeismogramFileReference;
import edu.sc.seis.sod.hibernate.SeismogramFileRefDB;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.UnsupportedFileTypeException;
import edu.sc.seis.sod.hibernate.eventpair.AbstractEventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;
import edu.sc.seis.sod.web.jsonapi.EventVectorJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.StationJson;
import edu.sc.seis.sod.web.jsonapi.WaveformJson;

public class EventVectorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        logger.info("GET: " + URL);

        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        Matcher m = eventStationPattern.matcher(URL);
        Matcher waveformMatcher = waveformPattern.matcher(URL);
        if (waveformMatcher.matches()) {

            WebAdmin.setJsonHeader(req, resp);
            AbstractEventChannelPair ecp = getECP(waveformMatcher.group(1));
            Channel[] chans;
            if (ecp instanceof EventVectorPair) {
                chans = ((EventVectorPair)ecp).getChannelGroup().getChannels();
            } else {
                chans = new Channel[] {((EventChannelPair)ecp).getChannel()};
            }
            List<EventSeismogramFileReference> seisRefList = new ArrayList<EventSeismogramFileReference>();
            for (int j = 0; j < chans.length; j++) {
                seisRefList.addAll(SeismogramFileRefDB.getSingleton()
                        .getSeismogramsForEventForChannel(ecp.getEvent(), chans[j]));
            }
            JsonApi.encodeJson(out, WaveformJson.toJsonList(seisRefList, WebAdmin.getApiBaseUrl()));

        } else if (m.matches()) {
            WebAdmin.setJsonHeader(req, resp);
            AbstractEventChannelPair ecp = getECP(m.group(1));
            EventVectorJson jsonData = new EventVectorJson(ecp, WebAdmin.getApiBaseUrl());
            JsonApi.encodeJson(out, jsonData);

        } else {
        	logger.warn("Bad URL for servlet: "+URL);
        	JsonApi.encodeError(out, "bad url for servlet: " + URL);
        	resp.sendError(500);
        }
    	writer.close();
        AbstractHibernateDB.rollback();
    }

    AbstractEventChannelPair getECP(String dbid) {
        Query q = AbstractHibernateDB.getSession().createQuery("from " + SodDB.getSingleton().getEcpClass().getName()
                + " where dbid = " + dbid);
        AbstractEventChannelPair esp = (AbstractEventChannelPair)q.uniqueResult();
        return esp;
    }

    Pattern eventStationPattern = Pattern.compile(".*/quake-vectors/([0-9]+)");

    Pattern waveformPattern = Pattern.compile(".*/quake-vectors/([0-9]+)/waveforms");
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EventVectorServlet.class);
}