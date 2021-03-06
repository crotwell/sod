package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.velocity.VelocityContext;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.ISOTime;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.display.configuration.BorderConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.BorderTitleConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.velocity.ContextWrangler;
import edu.sc.seis.sod.velocity.SimpleVelocitizer;

public class SeismogramTitler {

    public SeismogramTitler(BorderConfiguration titleBorder) {
        this.titleBorder = titleBorder;
        sv = new SimpleVelocitizer();
        BorderTitleConfiguration[] titles = titleBorder.getTitles();
        titleFormatStrings = new String[titles.length];
        for(int i = 0; i < titles.length; i++) {
            titleFormatStrings[i] = titles[i].getTitle();
        }
    }

    public void title(EventAccessOperations event,
                      Channel channel,
                      MicroSecondTimeRange timeRange) {
        title(event, channel, timeRange, null);
    }

    public void title(EventAccessOperations event,
                      Channel channel,
                      MicroSecondTimeRange timeRange,
                      Map<String, Object> extras) {
        VelocityContext vc = ContextWrangler.createContext(channel);
        ContextWrangler.insertIntoContext(channel, vc);
        ContextWrangler.insertIntoContext(event, vc);
        vc.put("beginTime", df.format(timeRange.getBeginTime()));
        vc.put("endTime", df.format(timeRange.getEndTime()));
        if (extras != null) {
            for (String key : extras.keySet()) {
                vc.put(key, extras.get(key));
            }
        }
        BorderTitleConfiguration[] titles = titleBorder.getTitles();
        for(int j = 0; j < titles.length; j++) {
            titles[j].setText(sv.evaluate(titleFormatStrings[j], vc));
        }
    }

    public static BorderConfiguration[] extractBorderConfigs(SeismogramDisplayConfiguration[] sdcs) {
        List borderConfigs = new ArrayList();
        for(int i = 0; i < sdcs.length; i++) {
            borderConfigs.add(sdcs[i].getBorders());
        }
        return (BorderConfiguration[])borderConfigs.toArray(new BorderConfiguration[0]);
    }
    
    private ThreadSafeSimpleDateFormat df = new ThreadSafeSimpleDateFormat("yyyy-MM-dd HH:mm:ss z", ISOTime.UTC);

    private SimpleVelocitizer sv;

    private String[] titleFormatStrings;

    private BorderConfiguration titleBorder;
}
