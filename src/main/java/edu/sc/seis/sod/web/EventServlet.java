package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.EventDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;

public class EventServlet extends HttpServlet {

    public EventServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: "+URL);
        Pattern singleEvent = Pattern.compile(".*/events/([0-9]+)");
        Matcher matcher = singleEvent.matcher(URL);
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        out.object();
        if (matcher.matches()) {
            String dbid = matcher.group(1);
            try {
                StatefulEvent e = StatefulEventDB.getSingleton().getEvent(Integer.parseInt(dbid));
                out.key("event");
                encodeJson(e, out);
                sideLoadOrigins(Collections.singletonList(e), out);
            } catch(NoPreferredOrigin eee) {
                // logger.error("No Preferred for event dbid: "+e.getDbid());
            } catch(NumberFormatException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch(NotFound e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } else {
            // logger.debug("doGet all");
            try {
                List<StatefulEvent> events = StatefulEventDB.getSingleton().getAll();
                out.key("events").array();
                for (StatefulEvent ve : events) {
                    encodeJson(ve, out);
                }
                out.endArray();
                sideLoadOrigins(events, out);
            } catch(JSONException e) {
                throw new ServletException(e);
            } catch(NoPreferredOrigin e) {
                throw new ServletException(e);
            }
        }
        out.endObject();
        writer.close();
    }

    public static void encodeJson(StatefulEvent ve, JSONWriter out) throws JSONException, NoPreferredOrigin {
        String name;
        if (ve.get_attributes().name != null & ve.get_attributes().name.length() > 0) {
            name = ve.get_attributes().name;
        } else {
            name = ve.getPreferred().getTime().getFissuresTime().date_time;
        }
        out.object()
                .key("id").value(ve.getDbid())
                .key("name")
                .value(name)
                .key("prefOrigin")
                .value(ve.getPreferred().getDbid())
                .key("prefMagnitude")
                .value(ve.getPreferred().getDbid() + "_" + ve.getPreferred().getMagnitudes()[0].type)
                .key("status").value(ve.getStatus().toString())
                .key("originList")
                .array();
        for (Origin o : ve.getOrigins()) {
            out.value(((OriginImpl)o).getDbid());
        }
        out.endArray();
        out.endObject();
    }

    public static void encodeJson(int eventId, OriginImpl origin, JSONWriter out) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        out.object()
                .key("id")
                .value(origin.getDbid())
                .key("event")
                .value(eventId)
                .key("time")
                .value(sdf.format(origin.getTime()))
                .key("lat")
                .value(origin.getLocation().latitude)
                .key("lon")
                .value(origin.getLocation().longitude)
                .key("depth")
                .value(((QuantityImpl)origin.getLocation().depth).getValue(UnitImpl.METER))
                .key("magnitudes")
                .array();
        Magnitude[] mags = origin.getMagnitudes();
        for (int i = 0; i < mags.length; i++) {
            out.value(origin.getDbid() + "_" + mags[i].type);
        }
        out.endArray();
        out.endObject();
    }

    public static void encodeJson(int eventId, int originId, Magnitude mag, JSONWriter out) {
        out.object()
                .key("id")
                .value(originId + "_" + mag.type)
                .key("type")
                .value(mag.type)
                .key("mag")
                .value(mag.value)
                .key("contributor")
                .value(mag.contributor);
        out.endObject();
    }

    public static void sideLoadOrigins(List<StatefulEvent> eqs, JSONWriter out) throws ServletException,
            IOException, JSONException, NoPreferredOrigin {
        out.key("origins").array();
        for (StatefulEvent ve : eqs) {
            Origin[] origins = ve.getOrigins();
            for (int j = 0; j < origins.length; j++) {
                encodeJson(ve.getDbid(), (OriginImpl)origins[j], out);
            }
        }
        out.endArray();
        out.key("magnitudes").array();
        for (StatefulEvent ve : eqs) {
            Origin[] origins = ve.getOrigins();
            for (int j = 0; j < origins.length; j++) {
                Magnitude[] mags = origins[j].getMagnitudes();
                for (int i = 0; i < mags.length; i++) {
                    encodeJson(ve.getDbid(), ((OriginImpl)origins[j]).getDbid(), mags[i], out);
                }
            }
        }
        out.endArray();
    }
}
