package edu.sc.seis.sod.subsetter.origin;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class OriginTimeRange implements OriginSubsetter, MicroSecondTimeRangeSupplier {

    public OriginTimeRange(Element config) throws ConfigurationException {
        begin = makeLoader(config, "startTime");
        end = makeLoader(config, "endTime");
    }

    private MicroSecondDateLoader makeLoader(Element config, String time)
            throws ConfigurationException {
        Element timeEl = DOMHelper.getElement(config, time);
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
            return new MicroSecondDateLoader() {

                public MicroSecondDate load() {
                    if(netElName.indexOf("Start") != -1) {
                        return ntr.getMSTR().getBeginTime();
                    } else {
                        return ntr.getMSTR().getEndTime();
                    }
                }
            };
        } else {
            final MicroSecondDate date = new MicroSecondDate(SodUtil.loadTime(timeEl,
                                                                              time.indexOf("end") != -1));
            return new MicroSecondDateLoader() {

                public MicroSecondDate load() {
                    return date;
                }
            };
        }
    }

    public synchronized MicroSecondTimeRange getMSTR() {
        if(range == null) {
            range = new MicroSecondTimeRange(begin.load(), end.load());
        }
        return range;
    }

    public StringTree accept(CacheEvent event, EventAttr eventAttr, Origin origin) {
        return new StringTreeLeaf(this, getMSTR().contains(new MicroSecondDate(origin.getOriginTime())));
    }

    public interface MicroSecondDateLoader {

        public MicroSecondDate load();
    }

    private MicroSecondTimeRange range;

    private MicroSecondDateLoader begin, end;
}// EventTimeRange
