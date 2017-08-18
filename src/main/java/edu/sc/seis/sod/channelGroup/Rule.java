package edu.sc.seis.sod.channelGroup;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.channel.PassChannel;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;
import edu.sc.seis.sod.subsetter.network.PassNetwork;
import edu.sc.seis.sod.subsetter.station.PassStation;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

public class Rule {

    public Rule(Element config, String ruleName) throws ConfigurationException {
        this.ruleName = ruleName;
        NodeList children = config.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node instanceof Element) {
                Element el = (Element)node;
                if (el.getTagName().equals("siteMatchRule")) {
                    siteChanRuleList.add(new SiteMatchRule(el));
                } else if (el.getTagName().equals("orientedSiteRule")) {
                        siteChanRuleList.add(new OrientedSiteRule(el));
                    } else {
                    Object sodElement = SodUtil.load(el, NetworkArm.PACKAGES);
                    if (sodElement instanceof NetworkSubsetter) {
                        attrSubsetter = (NetworkSubsetter)sodElement;
                    } else if (sodElement instanceof StationSubsetter) {
                        stationSubsetter = (StationSubsetter)sodElement;
                    } else if (sodElement instanceof ChannelSubsetter) {
                        chanSubsetter = (ChannelSubsetter)sodElement;
                    } else {
                        throw new ConfigurationException("Unknown configuration object: " + sodElement.getClass());
                    }
                }
            } // end of if (node instanceof Element)
        }
    }

    public List<ChannelGroup> acceptable(List<Channel> chanList, List<Channel> failures) {
        List<Channel> possible = new ArrayList<Channel>();
        for (Channel chan : chanList) {
            try {
                if (attrSubsetter.accept((Network)chan.getStation().getNetworkAttr()).isSuccess()) {
                    if (stationSubsetter.accept(chan.getStationImpl(), null).isSuccess()) {
                        if (chanSubsetter.accept(chan, null).isSuccess()) {
                            possible.add(chan);
                            continue;
                        }
                    }
                }
                failures.add(chan);
            } catch(Exception e) {
                // guess not
                logger.warn("Exception in channgeGrouper rule.", e);
                failures.add(chan);
            }
        }
        List<ChannelGroup> out = new ArrayList<ChannelGroup>();
        List<Channel> stillToTest = new ArrayList<Channel>();
        for (SiteChannelRule threeChar : siteChanRuleList) {
            out.addAll(threeChar.acceptable(possible, stillToTest));
            possible = stillToTest;
            stillToTest = new ArrayList<Channel>();
        }
        failures.addAll(possible);
        return out;
    }

    public String getRuleName() {
        return ruleName;
    }

    private String ruleName = "unknown";

    private NetworkSubsetter attrSubsetter = new PassNetwork();

    private StationSubsetter stationSubsetter = new PassStation();

    private ChannelSubsetter chanSubsetter = new PassChannel();

    private List<SiteChannelRule> siteChanRuleList = new ArrayList<SiteChannelRule>();

    public static final String[] PACKAGES = {"channel", "site", "station", "network"};

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Rule.class);
}
