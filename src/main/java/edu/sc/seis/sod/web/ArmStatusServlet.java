package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.web.jsonapi.ArmStatusJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;

public class ArmStatusServlet extends HttpServlet {

    public ArmStatusServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: " + URL);
        resp.setContentType("application/vnd.api+json");
        PrintWriter writer = resp.getWriter();
        JSONWriter out = new JSONWriter(writer);
        List<JsonApiData> json = new ArrayList<JsonApiData>();
        json.add(new ArmStatusJson(Start.getNetworkArm(), WebAdmin.getBaseUrl()));
        json.add(new ArmStatusJson(Start.getEventArm(), WebAdmin.getBaseUrl()));
        WaveformArm[] waveformArms = Start.getWaveformArms();
        for (int i = 0; i < waveformArms.length; i++) {
            json.add(new ArmStatusJson(waveformArms[i], WebAdmin.getBaseUrl()));
        }
        JsonApi.encodeJson(out, json);
        writer.close();
            resp.setStatus(HttpServletResponse.SC_OK);
    }
}
