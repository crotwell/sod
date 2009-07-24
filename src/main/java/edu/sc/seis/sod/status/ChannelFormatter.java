package edu.sc.seis.sod.status;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.Status;

public class ChannelFormatter extends Template implements ChannelTemplate {

    public ChannelFormatter() {
        templates = new ArrayList();
        templates.add(getTemplate("id", null));
    }

    public static ChannelFormatter makeSiteAndCode() {
        ChannelFormatter formatter = new ChannelFormatter();
        formatter.templates.clear();
        formatter.templates.add(formatter.getTemplate("siteCode", null));
        formatter.templates.add(formatter.textTemplate("."));
        formatter.templates.add(formatter.getTemplate("channelCode", null));
        return formatter;
    }

    public ChannelFormatter(Element el) throws ConfigurationException {
        this(el, null);
    }

    public ChannelFormatter(Element el, boolean filize)
            throws ConfigurationException {
        this(el, null, filize);
    }

    public ChannelFormatter(Element el, ChannelGroupTemplate cgt)
            throws ConfigurationException {
        this(el, cgt, false);
    }

    public ChannelFormatter(Element el, ChannelGroupTemplate cgt, boolean filize)
            throws ConfigurationException {
        this.cgt = cgt;
        parse(el, filize);
        filizeResults = filize;
    }

    protected Object textTemplate(final String text) {
        return new ChannelTemplate() {

            public String getResult(Channel chan) {
                return text;
            }
        };
    }

    protected Object getTemplate(String tag, final Element el) {
        if(tag.equals("stationCode")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return chan.get_id().station_code;
                }
            };
        } else if(tag.equals("channelCode")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return chan.get_id().channel_code;
                }
            };
        } else if(tag.equals("networkCode")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return chan.get_id().network_id.network_code;
                }
            };
        } else if(tag.equals("stationCode")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return chan.get_code();
                }
            };
        } else if(tag.equals("siteCode")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    if(chan.get_id().site_code.equals("  ") && filizeResults) {
                        return "__";
                    }
                    return chan.get_id().site_code;
                }
            };
        } else if(tag.equals("beginTime")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return btt.getResult(chan.get_id().begin_time);
                }

                TimeTemplate btt = new TimeTemplate(el, false);
            };
        } else if(tag.equals("endTime")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return btt.getResult(chan.getEndTime());
                }

                TimeTemplate btt = new TimeTemplate(el, false);
            };
        } else if(tag.equals("beginTimeUnformatted")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return chan.get_id().begin_time.date_time;
                }
            };
        } else if(tag.equals("dip")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return format(chan.getOrientation().dip);
                }
            };
        } else if(tag.equals("azimuth")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return format(chan.getOrientation().azimuth);
                }
            };
        } else if(tag.equals("name")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return chan.getName();
                }
            };
        } else if(tag.equals("lat")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return format(chan.getSite().getLocation().latitude);
                }
            };
        } else if(tag.equals("lon")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return format(chan.getSite().getLocation().longitude);
                }
            };
        } else if(tag.equals("lon")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return format(chan.getSite().getLocation().longitude);
                }
            };
        } else if(tag.equals("status") && cgt != null) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return cgt.getStatus(chan);
                }
            };
        } else if(tag.equals("standing") && cgt != null) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    Status status = (Status)cgt.channelMap.get(chan);
                    return status.getStanding().toString();
                }
            };
        } else if(tag.equals("id")) {
            return new ChannelTemplate() {

                public String getResult(Channel chan) {
                    return ChannelIdUtil.toString(chan.get_id());
                }
            };
        }
        return super.getCommonTemplate(tag, el);
    }

    private String format(double d) {
        synchronized(formatter) {
            return formatter.format(d);
        }
    }

    private DecimalFormat formatter = new DecimalFormat("#.#");

    public String getResult(Channel chan) {
        StringBuffer buf = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()) {
            ChannelTemplate cur = (ChannelTemplate)it.next();
            buf.append(cur.getResult(chan));
        }
        if(filizeResults) {
            return FissuresFormatter.filize(buf.toString());
        } else {
            return buf.toString();
        }
    }

    boolean filizeResults = false;

    ChannelGroupTemplate cgt;
}
