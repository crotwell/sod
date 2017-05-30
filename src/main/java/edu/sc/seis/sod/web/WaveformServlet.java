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

import edu.sc.seis.sod.hibernate.AbstractHibernateDB;
import edu.sc.seis.sod.hibernate.EventSeismogramFileReference;
import edu.sc.seis.sod.hibernate.SeismogramFileRefDB;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.UnsupportedFileTypeException;
import edu.sc.seis.sod.hibernate.eventpair.AbstractEventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventChannelPair;
import edu.sc.seis.sod.hibernate.eventpair.EventVectorPair;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.web.jsonapi.JsonApi;

public class WaveformServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        logger.info("GET: " + URL);
        Matcher m = mseedPattern.matcher(URL);
        if (m.matches()) {
            // raw miniseed
            AbstractEventChannelPair ecp = getECP(m.group(1));
            ChannelImpl[] chans;
            if (ecp instanceof EventVectorPair) {
                chans = ((EventVectorPair)ecp).getChannelGroup().getChannels();
            } else {
                chans = new ChannelImpl[] {((EventChannelPair)ecp).getChannel()};
            }
            List<EventSeismogramFileReference> seisRefList = new ArrayList<EventSeismogramFileReference>();
            for (int j = 0; j < chans.length; j++) {
                seisRefList.addAll(SeismogramFileRefDB.getSingleton()
                        .getSeismogramsForEventForChannel(ecp.getEvent(), chans[j].getId()));
            }
            resp.setContentType("application/vnd.fdsn.mseed");
            OutputStream outBinary = resp.getOutputStream();
            logger.info("SeisFileRef size: "+seisRefList.size());
            for (EventSeismogramFileReference ref : seisRefList) {
                logger.info("FileRef: "+ref.getFilePath());
                try {
                    if (SeismogramFileTypes.fromInt(ref.getFileType()).equals(SeismogramFileTypes.MSEED)) {
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
            }
            outBinary.flush();
        } else {
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            JsonApi.encodeError(out, "url does not match " + mseedPattern.pattern());
            resp.sendError(500);
            writer.close();
        }
        AbstractHibernateDB.rollback();
    }

    AbstractEventChannelPair getECP(String dbid) {
        Query q = AbstractHibernateDB.getSession().createQuery("from " + SodDB.getSingleton().getEcpClass().getName()
                + " where dbid = " + dbid);
        AbstractEventChannelPair esp = (AbstractEventChannelPair)q.uniqueResult();
        return esp;
    }

    Pattern mseedPattern = Pattern.compile(".*/waveforms?/([0-9]+)");
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WaveformServlet.class);
}
