package edu.sc.seis.sod.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONWriter;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.AbstractEventChannelPair;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.EventVectorPair;
import edu.sc.seis.sod.SodConfig;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.WaveformArm;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.eventArm.EventMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import edu.sc.seis.sod.web.jsonapi.ArmStatusJson;
import edu.sc.seis.sod.web.jsonapi.JsonApi;
import edu.sc.seis.sod.web.jsonapi.JsonApiData;

public class ArmStatusServlet extends HttpServlet {

    public ArmStatusServlet() {
        Start.getEventArm().add(eventMon);
        Start.getWaveformRecipe().addStatusMonitor(waveformMon);
    }

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
        Matcher matcher = armsPattern.matcher(URL);
        if (matcher.matches()) {
            List<JsonApiData> json = new ArrayList<JsonApiData>();
            json.add(new ArmStatusJson(Start.getNetworkArm(), WebAdmin.getBaseUrl()));
            json.add(new ArmStatusJson(Start.getEventArm(), WebAdmin.getBaseUrl()));
            WaveformArm[] waveformArms = Start.getWaveformArms();
            for (int i = 0; i < waveformArms.length; i++) {
                json.add(new ArmStatusJson(waveformArms[i], WebAdmin.getBaseUrl()));
            }
            JsonApi.encodeJson(out, json);
        } else {
            matcher = recipePattern.matcher(URL);
            if (matcher.matches()) {
                SodConfig config = SodDB.getSingleton().getCurrentConfig();
                String recipe = config.getConfig();
            }
        }
        writer.close();
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    
    EventMonitor eventMon = new EventMonitor() {
        
        String status = "unknown";
        CacheEvent lastEvent;
        Status lastEventStatus;
        
        @Override
        public void setArmStatus(String status) throws Exception {
            this.status = status;
        }

        @Override
        public void change(CacheEvent event, Status status) {
            this.lastEvent = event;
            this.lastEventStatus = status;
        }
        
    };
    
    WaveformMonitor waveformMon = new ThreadAwareWaveformMonitor();

    Pattern armsPattern = Pattern.compile(".*/arms");
    Pattern recipePattern = Pattern.compile(".*/arms/([^/]+)/recipe");
    
}

class ThreadAwareWaveformMonitor implements WaveformMonitor {
    
    
    ThreadLocal<WaveformMonitor> monitorsByThread = new ThreadLocal<WaveformMonitor>() {

        @Override
        protected WaveformMonitor initialValue() {
            return new LastWaveformStatus();
        }
        
    };

    @Override
    public void update(EventNetworkPair ecp) {
        monitorsByThread.get().update(ecp);
    }

    @Override
    public void update(EventStationPair ecp) {
        monitorsByThread.get().update(ecp);
    }

    @Override
    public void update(EventChannelPair ecp) {
        monitorsByThread.get().update(ecp);
    }

    @Override
    public void update(EventVectorPair evp) {
        monitorsByThread.get().update(evp);
    }
}

class LastWaveformStatus implements WaveformMonitor {
    
    AbstractEventChannelPair lastECP;
    EventStationPair lastEventStation;
    EventNetworkPair lastEventNetwork;
    
    @Override
    public void update(EventVectorPair evp) {
        this.lastECP = evp;
    }
    
    @Override
    public void update(EventChannelPair ecp) {
        this.lastECP = ecp;
    }
    
    @Override
    public void update(EventStationPair ecp) {
        this.lastEventStation = ecp;
    }
    
    @Override
    public void update(EventNetworkPair ecp) {
        this.lastEventNetwork = ecp;
    }
}
