package edu.sc.seis.sod.status;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.event.MagnitudeUtil;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.chooser.ThreadSafeSimpleDateFormat;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.status.eventArm.EventTemplate;

public class EventFormatter extends Template implements EventTemplate {

    public EventFormatter(boolean filize) throws ConfigurationException {
        this(null, filize);
    }

    public EventFormatter(Element config) throws ConfigurationException {
        this(config, false);
    }

    public static EventFormatter makeTime() {
        try {
            EventFormatter formatter = new EventFormatter(false);
            formatter.templates.clear();
            formatter.templates.add(formatter.getTemplate("originTime", null));
            return formatter;
        } catch(ConfigurationException e) {
            throw new RuntimeException("Shouldn't blow up with a default formatter",
                                       e);
        }
    }

    public static EventFormatter makeFilizer() {
        try {
            return new EventFormatter(null, true);
        } catch(ConfigurationException e) {
            throw new RuntimeException("Shouldn't blow up with a default formatter",
                                       e);
        }
    }

    public EventFormatter(Element config, boolean filize)
            throws ConfigurationException {
        if(config == null || config.hasChildNodes() == false)
            useDefaultConfig();
        else
            parse(config, filize);
        filizeResults = filize;
    }

    public static EventFormatter getDefaultFormatter() {
        try {
            if(defaultFormatter == null)
                defaultFormatter = new EventFormatter(false);
        } catch(ConfigurationException e) {
            // this should never happen as default is constructed with a null
            // element
            GlobalExceptionHandler.handle("This should never have happened, there is a bug in the default EventFormater.",
                                          e);
            throw new RuntimeException("Got configuration exception for default event formater",
                                       e);
        }
        return defaultFormatter;
    }

    public static String getDefaultResult(CacheEvent event) {
        return defaultFormatter.getResult(event);
    }

    private static EventFormatter defaultFormatter;

    protected Object textTemplate(final String text) {
        return new EventTemplate() {

            public String getResult(CacheEvent ev) {
                return text;
            }
        };
    }

    protected Object getTemplate(String tag, Element el) {
        if(tag.equals("feRegionName")) {
            return new RegionName();
        } else if(tag.equals("feRegionNumber")) {
            return new EventTemplate() {

                public String getResult(CacheEvent ev) {
                    return Integer.toString(ev.get_attributes().region.number);
                }
            };
        } else if(tag.equals("depth")) {
            return new EventTemplate() {

                public String getResult(CacheEvent ev) {
                    return UnitDisplayUtil.formatQuantityImpl(getOrigin(ev).getLocation().depth);
                }
            };
        } else if(tag.equals("latitude")) {
            return new EventTemplate() {

                public String getResult(CacheEvent ev) {
                    return format(getOrigin(ev).getLocation().latitude);
                }
            };
        } else if(tag.equals("longitude")) {
            return new EventTemplate() {

                public String getResult(CacheEvent ev) {
                    return format(getOrigin(ev).getLocation().longitude);
                }
            };
        } else if(tag.equals("magnitude")) {
            return new MagnitudeTemplate();
        } else if(tag.equals("allMagnitudes")) {
            return new EventTemplate() {

                public String getResult(CacheEvent ev) {
                    return getMags(ev);
                }
            };
        } else if(tag.equals("originTime")) {
            if(el == null) {
                return new Time();
            }
            return new Time(SodUtil.getText((el)));
        } else if(tag.equals("eventStatus")) {
            return new EventStatusFormatter();
        } else if(tag.equals("waveformChannels")) {
            try {
                return new EventChannelQuery(SodUtil.getNestedText(el));
            } catch(NoSuchFieldException e) {
                GlobalExceptionHandler.handle("Unknown standing name passed into event formatter",
                                              e);
            }
        }
        return super.getCommonTemplate(tag, el);
    }

    private class EventChannelQuery implements EventTemplate {

        public EventChannelQuery(String standing) throws NoSuchFieldException {
            s = Standing.getForName(standing);
        }

        public String getResult(CacheEvent ev) {
            if(s.equals(Standing.SUCCESS)) {
                return ""+SodDB.getSingleton().getNumSuccessful(ev);
            } else if(s.equals(Standing.REJECT)) {
                return ""+SodDB.getSingleton().getNumFailed(ev);
            } else if(s.equals(Standing.RETRY)) {
                return ""+SodDB.getSingleton().getNumRetry(ev);
            }
            throw new RuntimeException("Should never happen: standing="+s);
        }

        private Standing s;
    }

    public static String format(double d) {
        return defaultDecimalFormat.format(d);
    }

    private static DecimalFormat defaultDecimalFormat = new DecimalFormat("#.#");

    private class MagnitudeTemplate implements EventTemplate {

        public String getResult(CacheEvent ev) {
            return getMag(ev);
        }
    };

    private class RegionName implements EventTemplate {

        public String getResult(CacheEvent ev) {
            return getRegionName(ev);
        }
    }

    private class Time implements EventTemplate {

        public Time() {
            this("yyyyMMdd'T'HH:mm:ss.SSS");
        }

        public Time(String format) {
            sdf = new ThreadSafeSimpleDateFormat(format, TimeZone.getTimeZone("GMT"));
        }

        public String getResult(CacheEvent ev) {
            try {
                return sdf.format(new MicroSecondDate(getOrigin(ev).getOriginTime()));
            } catch(NumberFormatException e) {
                throw new RuntimeException("Offending date_time: "
                        + getOrigin(ev).getOriginTime().date_time, e);
            }
        }

        private ThreadSafeSimpleDateFormat sdf;
    }

    private class EventStatusFormatter implements EventTemplate {

        public String getResult(CacheEvent e) {
            if(e instanceof StatefulEvent) {
                return "" + ((StatefulEvent)e).getStatus();
            } else {
                return "unknown";
            }
        }
    }

    public void useDefaultConfig() {
        templates.add(new RegionName());
        templates.add(textTemplate("_"));
        templates.add(new Time());
    }

    public synchronized String getResult(CacheEvent event) {
        StringBuffer name = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()) {
            name.append(((EventTemplate)it.next()).getResult(event));
        }
        if(filizeResults) {
            return FissuresFormatter.filize(name.toString());
        } else {
            return name.toString();
        }
    }

    public String getFilizedName(CacheEvent event) {
        return FissuresFormatter.filize(getResult(event));
    }

    private String getMags(CacheEvent event) {
        Magnitude[] mags = getOrigin(event).getMagnitudes();
        String result = new String();
        for(int i = 0; i < mags.length; i++) {
            result += MagnitudeUtil.toString(mags[i]) + " ";
        }
        return result;
    }

    public static String getMag(CacheEvent event) {
        Magnitude[] mags = getOrigin(event).getMagnitudes();
        if(mags.length > 0) {
            return MagnitudeUtil.toString(mags[0]);
        }
        throw new IllegalArgumentException("No magnitudes on event");
    }

    public static Origin getOrigin(CacheEvent event) {
        return EventUtil.extractOrigin(event);
    }

    public static String getRegionName(CacheEvent event) {
        return regions.getRegionName(event.get_attributes().region);
    }

    private boolean filizeResults;

    static ParseRegions regions = ParseRegions.getInstance();

    private static Logger logger = LoggerFactory.getLogger(EventFormatter.class);
} // NameGenerator
