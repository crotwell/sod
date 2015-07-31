package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.WaveformArm;


public class ArmStatusServlet extends HttpServlet {

    public ArmStatusServlet() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String URL = req.getRequestURL().toString();
        System.out.println("GET: "+URL);
        if (req.getRequestURI().endsWith(".html")) {
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hello from ArmStatusServlet</h1>");
        out.println("<p>NetworkArm: "+Start.getNetworkArm().isActive()+"</p>");
        out.println("<p>EventArm: "+Start.getNetworkArm().isActive()+"</p>");
        WaveformArm[] waveformArms = Start.getWaveformArms();
        for (int i = 0; i < waveformArms.length; i++) {
            out.println("<p>WaveformArm"+i+": "+waveformArms[i].isActive()+"</p>");
        }
        out.println("</body></html>");
        } else {
            // json
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            JSONWriter out = new JSONWriter(writer);
            out.object();
            out.key("arms").array();
            out.object().key("id").value("eventArm")
            .key("alive").value(Start.getEventArm().isActive())
            .endObject();
            out.object().key("id").value("networkArm")
            .key("alive").value(Start.getNetworkArm().isActive())
            .endObject();
            WaveformArm[] waveformArms = Start.getWaveformArms();
            for (int i = 0; i < waveformArms.length; i++) {
                out.object().key("id").value("waveformArm"+i)
                .key("alive").value(waveformArms[i].isActive())
                .endObject();
            }
            out.endArray();
            out.endObject();
            writer.close();
        }
        
    }
    
    
}
