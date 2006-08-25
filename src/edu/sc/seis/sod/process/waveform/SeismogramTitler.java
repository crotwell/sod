package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.display.configuration.BorderConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.BorderTitleConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.SeismogramDisplayConfiguration;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.ChannelFormatter;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.GenericTemplate;
import edu.sc.seis.sod.status.StationFormatter;

public class SeismogramTitler {

    public SeismogramTitler(SeismogramDisplayConfiguration[] sdc) {
        this.sdc = sdc;
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
                } else {
                    formatters.add(new GenericTemplate() {

                        public String getResult() {
                            return child.getTextContent();
                        }
                    });
                }
            }
        }
    }

    public void title(EventAccessOperations event, Channel channel) {
        System.out.println("titler.title called");
        List borderList = new ArrayList();
        for(int i = 0; i < sdc.length; i++) {
            borderList.addAll(Arrays.asList(sdc[i].getBorders()));
        }
        BorderConfiguration[] borders = (BorderConfiguration[])borderList.toArray(new BorderConfiguration[0]);
        for(int i = 0; i < borders.length; i++) {
            BorderTitleConfiguration[] titles = borders[i].getTitles();
            for(int j = 0; j < titles.length; j++) {
                if(titles[j].getId().equals(titleId)) {
                    System.out.println("titler found title id: " + titleId);
                    titles[j].setText(getResults(event, channel));
                }
            }
        }
    }

    private String getResults(EventAccessOperations event, Channel channel) {
        StringBuffer buf = new StringBuffer();
        for(int i = 0; i < formatters.size(); i++) {
            Object o = formatters.get(i);
            if(o instanceof StationFormatter) {
                buf.append(((StationFormatter)o).getResult(channel.my_site.my_station));
            } else if(o instanceof ChannelFormatter) {
                buf.append(((ChannelFormatter)o).getResult(channel));
            } else if(o instanceof EventFormatter) {
                buf.append(((EventFormatter)o).getResult(event));
            } else {
                buf.append(((GenericTemplate)o).getResult());
            }
        }
        System.out.println("titler results: " + buf.toString());
        return buf.toString();
    }

    private SeismogramDisplayConfiguration[] sdc;

    private String titleId;

    private List formatters = new ArrayList();
}
