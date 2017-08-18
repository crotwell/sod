package edu.sc.seis.sod.source.seismogram;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;

public class DownloadStatistics implements SeismogramSourceLocator {

    public DownloadStatistics(Element config) throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for (int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if (node instanceof Element) {
                SodElement sodElement = (SodElement)SodUtil.load((Element)node, new String[] {"seismogram"});
                if (sodElement instanceof SeismogramSourceLocator) {
                    wrapped = (SeismogramSourceLocator)sodElement;
                }
            } // end of else
        }
        Timer t = new Timer("Download Stats", true);
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                System.out.println(summarizeStats());
            }
        },
                   5000,
                   5000);
        runBegin = System.currentTimeMillis();
    }

    @Override
    public SeismogramSource getSeismogramSource(final CacheEvent event,
                                                final Channel channel,
                                                final RequestFilter[] infilters,
                                                final CookieJar cookieJar) throws Exception {
        return new SeismogramSource() {

            SeismogramSource wrappedSource = wrapped.getSeismogramSource(event, channel, infilters, cookieJar);

            @Override
            public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
                ElapsedTime elapsed = new ElapsedTime();
                List<LocalSeismogramImpl> out = wrappedSource.retrieveData(request);
                elapsed.end();
                int dataPoints = 0;
                for (LocalSeismogramImpl ls : out) {
                    dataPoints += ls.getNumPoints();
                }
                elapsed.setNpts(dataPoints);
                addElapsed(elapsed);
                return out;
            }
        };
    }

    void addElapsed(ElapsedTime elapsed) {
        synchronized (lastDownloads) {
            totalPoints += elapsed.npts;
            lastDownloads.addFirst(elapsed);
            if (lastDownloads.size() > maxElapsedListSize) {
                lastDownloads.removeLast();
            }
            if (best == null || elapsed.getKiloSamplesPerSec() > best.getKiloSamplesPerSec()) {
                best = elapsed;
            }
            if (worst == null || elapsed.getKiloSamplesPerSec() < worst.getKiloSamplesPerSec()) {
                worst = elapsed;
            }
        }
    }
    
    public String summarizeStats() {
        long timeTotal = 0;
        int nptsTotal = 0;
        long wallTime;
        long totalRunTime = System.currentTimeMillis()-runBegin;
        synchronized (lastDownloads) {
            for (ElapsedTime e : lastDownloads) {
                timeTotal += e.getElapsed();
                nptsTotal += e.npts;
            }
            wallTime = lastDownloads.getFirst().end-lastDownloads.getLast().begin;
        }
        String out = "Last "+maxElapsedListSize+": download="+nptsTotal*1f/timeTotal+" wall="+nptsTotal*1f/wallTime+"\n";
        out += "Best: "+best.getKiloSamplesPerSec()+"  Worst: "+worst.getKiloSamplesPerSec()+"\n";
        out += "Total Runtime: "+totalRunTime*1f/totalPoints+"  over "+totalRunTime/1000+" sec";
        return out;
    }
    
    ElapsedTime best, worst;
    
    LinkedList<ElapsedTime> lastDownloads = new LinkedList<ElapsedTime>();

    int maxElapsedListSize = 10;

    long runBegin;
    long totalPoints = 0;
    
    SeismogramSourceLocator wrapped;
}

class ElapsedTime {
    ElapsedTime() {
        this .begin = System.currentTimeMillis();
    }
    void end() {
        end = System.currentTimeMillis();
    }
    void setNpts(int npts) {
        this.npts = npts;
    }
    long begin;
    long end;
    int npts;
    long getElapsed() {
        return end-begin;
    }
    float getKiloSamplesPerSec() {
        return 1.0f*(npts)/(end-begin);  // samples per millisec == kilo per sec
    }
}
