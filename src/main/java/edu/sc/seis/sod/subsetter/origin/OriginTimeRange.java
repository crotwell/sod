package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;

import edu.iris.Fissures.event.EventAttrImpl;
import edu.iris.Fissures.event.OriginImpl;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.time.MicroSecondTimeRange;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.MicroSecondDateSupplier;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OriginTimeRange implements OriginSubsetter, MicroSecondTimeRangeSupplier {

    public OriginTimeRange(Element config) throws ConfigurationException {
        begin = makeLoader(config, "startTime");
        end = makeLoader(config, "endTime");
    }

    private MicroSecondDateSupplier makeLoader(Element config, String time)
            throws ConfigurationException {
        Element timeEl = DOMHelper.getElement(config, time);
        if (timeEl == null && time.indexOf("end") != -1) {
            // if no endtime, use <future/>
            return new MicroSecondDateSupplier() {
                public MicroSecondDate load() {
                    return ClockUtil.wayFuture();
                }
            };
        }
        Element network = null;
        for(int i = 0; i < timeEl.getChildNodes().getLength(); i++) {
            if(timeEl.getChildNodes().item(i) instanceof Element) {
                if(timeEl.getChildNodes().item(i).getLocalName().startsWith("network")) {
                    network = (Element)timeEl.getChildNodes().item(i);
                    break;
                }
            }
        }
        if(network != null) {
            final String netElName = network.getLocalName();
            final NetworkTimeRange ntr = new NetworkTimeRange();
            return new MicroSecondDateSupplier() {

                public MicroSecondDate load() {
                    if(netElName.indexOf("Start") != -1) {
                        return ntr.getMSTR().getBeginTime();
                    } else {
                        return ntr.getMSTR().getEndTime();
                    }
                }
            };
        } else {
            return SodUtil.loadTime(timeEl, time.indexOf("end") != -1);
        }
    }

    // should this float????
    public synchronized MicroSecondTimeRange getMSTR() {
        return new MicroSecondTimeRange(begin.load(), end.load());
    }

    public StringTree accept(CacheEvent event, EventAttrImpl eventAttr, OriginImpl origin) {
        return new StringTreeLeaf(this, getMSTR().contains(new MicroSecondDate(origin.getOriginTime())));
    }

    private MicroSecondDateSupplier begin, end;
}// EventTimeRange
