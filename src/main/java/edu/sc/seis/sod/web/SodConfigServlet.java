package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import edu.sc.seis.sod.SodConfig;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.SodConfigJson;

public class SodConfigServlet  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SodConfig config = SodDB.getSingleton().getCurrentConfig();
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
        JsonApi.encodeJson(out, new SodConfigJson(config, WebAdmin.getBaseUrl()));
        resp.setStatus(HttpServletResponse.SC_OK);
        writer.close();
    }
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SodConfigServlet.class);
}
