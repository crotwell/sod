package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.sc.seis.TauP.Arrival;
import edu.sc.seis.TauP.TauModelException;
import edu.sc.seis.fissuresUtil.bag.TauPUtil;
import edu.sc.seis.fissuresUtil.hibernate.NetworkDB;
import edu.sc.seis.sod.web.jsonapi.TauPJson;


public class TauPServlet  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
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
            }
            writer.close();
        } catch(TauModelException e) {
            throw new ServletException(e);
        } catch(JSONException e) {
            throw new ServletException(e);
        } catch(NumberFormatException e) {
            throw new ServletException(e);
        } finally {
            NetworkDB.rollback();
        }
    }
    
    public static final String MODEL = "model";
    public static final String EVDEPTH = "evdepth";
    public static final String PHASES = "phases";
    public static final String[] DEFAULT_PHASES = {"p", "s", "P", "S"};
    public static final String DISTDEG = "distdeg";
}
