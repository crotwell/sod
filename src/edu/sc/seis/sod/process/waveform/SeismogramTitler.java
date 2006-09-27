package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.configuration.BorderConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.BorderTitleConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.ChannelFormatter;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.MicroSecondTimeRangeFormatter;
import edu.sc.seis.sod.status.StationFormatter;

public class SeismogramTitler {

    public SeismogramTitler(BorderConfiguration titleBorder) {
        this.titleBorder = titleBorder;
    }

    public void configure(Element el) throws ConfigurationException {
        titleId = SodUtil.getText(SodUtil.getElement(el, "titleId"));
        configureFormatters(SodUtil.getElement(el, "template"));
    }

    private void configureFormatters(Element templateConfig)
            throws ConfigurationException {
        NodeList children = templateConfig.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if(child instanceof Element) {
                if(child.getNodeName().equals("station")) {
                    formatters.add(new StationFormatter((Element)child));
                } else if(child.getNodeName().equals("channel")) {
                    formatters.add(new ChannelFormatter((Element)child));
                } else if(child.getNodeName().equals("event")) {
                    formatters.add(new EventFormatter((Element)child));
                } else if(child.getNodeName().equals("timeWindow")) {
                    formatters.add(new MicroSecondTimeRangeFormatter((Element)child));
                } else {
                    formatters.add(new GenericTemplate() {

                        public String getResult() {
                            return child.getNodeValue();
                        }
                    });
                }
            }
        }
    }

    public void title(EventAccessOperations event,
                      Channel channel,
                      MicroSecondTimeRange timeRange) {
        BorderTitleConfiguration[] titles = titleBorder.getTitles();
        for(int j = 0; j < titles.length; j++) {
            if(titles[j].getId().equals(titleId)) {
                titles[j].setText(getResults(event, channel, timeRange));
            }
        }
    }

    private String getResults(EventAccessOperations event,
                              Channel channel,
                              MicroSecondTimeRange timeRange) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < formatters.size(); i++) {
            Object o = formatters.get(i);
            if(o instanceof StationFormatter) {
                buf.append(((StationFormatter)o).getResult(channel.my_site.my_station));
            } else if(o instanceof ChannelFormatter) {
                buf.append(((ChannelFormatter)o).getResult(channel));
            } else if(o instanceof EventFormatter) {
                buf.append(((EventFormatter)o).getResult(event));
            } else if(o instanceof MicroSecondTimeRangeFormatter) {
                buf.append(((MicroSecondTimeRangeFormatter)o).getResult(timeRange));
            } else {
                buf.append(((GenericTemplate)o).getResult());
            }
        }
        return buf.toString();
    }

    public static BorderConfiguration[] extractBorderConfigs(SeismogramDisplayConfiguration[] sdcs) {
        List borderConfigs = new ArrayList();
        for(int i = 0; i < sdcs.length; i++) {
            borderConfigs.add(sdcs[i].getBorders());
        }
        return (BorderConfiguration[])borderConfigs.toArray(new BorderConfiguration[0]);
    }

    private String titleId;

    private BorderConfiguration titleBorder;

    private List formatters = new ArrayList();
}
