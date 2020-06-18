package edu.sc.seis.sod.web;

import java.io.BufferedInputStream;
import java.io.File;
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
import edu.sc.seis.sod.web.jsonapi.WaveformJson;

public class WaveformServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        logger.info("GET: " + URL);
        Matcher waveformMatcher = waveformPattern.matcher(URL);
        Matcher m = mseedPattern.matcher(URL);
        if (m.matches()) {
            // raw miniseed
        	EventSeismogramFileReference ref = getSeisRef( m.group(1));
        	
            resp.setContentType("application/vnd.fdsn.mseed");
            OutputStream outBinary = resp.getOutputStream();
            logger.info("FileRef: "+ref.getFilePath());
            try {
            	if (SeismogramFileTypes.fromInt(ref.getFileType()).equals(SeismogramFileTypes.MSEED)) {
            		File seisFile = new File(ref.getFilePath());
            		resp.setHeader("content-disposition", seisFile.getName());
            		BufferedInputStream bufIn = new BufferedInputStream(ref.getFilePathAsURL().openStream());
            		byte[] buf = new byte[1024];
            		int bufNum = 0;
            		while ((bufNum = bufIn.read(buf)) != -1) {
            			outBinary.write(buf, 0, bufNum);
            		}
            		bufIn.close();
            	} else {
            		logger.warn("Not miniseed: "+ref.getFileType());
            		PrintWriter writer = resp.getWriter();
            		JSONWriter out = new JSONWriter(writer);
            		JsonApi.encodeError(out, "Waveform data not miniseed: "+ref.getFileType());
            		resp.sendError(500);
            		writer.close();
            	}
            } catch(UnsupportedFileTypeException e) {
            	throw new RuntimeException("Should never happen", e);
            }
            
            outBinary.flush();
        } else if (waveformMatcher.matches()) {
            WebAdmin.setJsonHeader(req, resp);
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            
        	EventSeismogramFileReference ref = getSeisRef( waveformMatcher.group(1));
        	WaveformJson jsonData = new WaveformJson(ref, WebAdmin.getApiBaseUrl());
            JsonApi.encodeJson(out, jsonData);
            writer.close();
        } else {
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            JsonApi.encodeError(out, "url does not match " + mseedPattern.pattern());
            resp.sendError(500);
            writer.close();
        }
        AbstractHibernateDB.rollback();
    }

    EventSeismogramFileReference getSeisRef(String dbid) {
        Query q = AbstractHibernateDB.getSession().createQuery("from " + EventSeismogramFileReference.class.getName()
                + " where dbid = " + dbid);
        EventSeismogramFileReference ref = (EventSeismogramFileReference)q.uniqueResult();
        return ref;
    }

    Pattern waveformPattern = Pattern.compile(".*/waveforms?/([0-9]+)");
    
    Pattern mseedPattern = Pattern.compile(".*/waveforms?/([0-9]+)/mseed");
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WaveformServlet.class);
}
