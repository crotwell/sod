package edu.sc.seis.sod.status;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.ConfigurationException;

public class MicroSecondTimeRangeFormatter extends Template implements
        MicroSecondTimeRangeTemplate {

    public MicroSecondTimeRangeFormatter(Element el)
            throws ConfigurationException {
        this(el, false);
    }

    public MicroSecondTimeRangeFormatter(Element el, boolean fileize)
            throws ConfigurationException {
        this.filizeResults = fileize;
        parse(el, filizeResults);
    }

    protected Object textTemplate(final String text) {
        return new MicroSecondTimeRangeTemplate() {

            public String getResult(MicroSecondTimeRange timeRange) {
                return text;
            }
        };
    }

    protected Object getTemplate(String tag, final Element el) {
        if(tag.equals("beginTime")) {
            return new MicroSecondTimeRangeTemplate() {

                public String getResult(MicroSecondTimeRange timeRange) {
                    return tt.getResult(timeRange.getBeginTime()
                            .getFissuresTime());
                }

                TimeTemplate tt = new TimeTemplate(el, true);
            };
        } else if(tag.equals("endTime")) {
            return new MicroSecondTimeRangeTemplate() {

                public String getResult(MicroSecondTimeRange timeRange) {
                    return tt.getResult(timeRange.getEndTime()
                            .getFissuresTime());
                }

                TimeTemplate tt = new TimeTemplate(el, true);
            };
        }
        return super.getCommonTemplate(tag, el);
    }

    public String getResult(MicroSecondTimeRange timeRange) {
        StringBuffer buf = new StringBuffer();
        Iterator it = templates.iterator();
        while(it.hasNext()) {
            buf.append(((MicroSecondTimeRangeTemplate)it.next()).getResult(timeRange));
        }
        if(filizeResults) {
            return FissuresFormatter.filize(buf.toString());
        } else {
            return buf.toString();
        }
    }

    boolean filizeResults = false;
}
