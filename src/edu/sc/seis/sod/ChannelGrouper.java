/**
 * ChannelGrouper.java
 * 
 * @author Jagadeesh Danala
 * @version
 */
package edu.sc.seis.sod;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.SamplingImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.sc.seis.fissuresUtil.bag.OrientationUtil;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.subsetter.channel.ChannelSubsetter;
import edu.sc.seis.sod.subsetter.network.NetworkSubsetter;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;
import edu.sc.seis.sod.validator.Validator;

public class ChannelGrouper {

    public ChannelGrouper() {
        this(null);
    }

    public ChannelGrouper(String configFileLoc) {
        defaultRules = loadRules(defaultConfigFileLoc);
        additionalRules = loadRules(configFileLoc);
    }

    public ChannelGroup[] group(Channel[] channels, List failures) {
        int ruleCount = defaultRules.length + additionalRules.length;
        Element[] allRules = new Element[ruleCount];
        for(int j = 0; j < additionalRules.length; j++) {
            allRules[j] = additionalRules[j];
        }
        for(int k = additionalRules.length; k < ruleCount; k++) {
            allRules[k] = defaultRules[k - additionalRules.length];
        }
        return applyRules(channels, allRules, failures);
    }

    private ChannelGroup[] applyRules(Channel[] channels,
                                      Element[] rules,
                                      List failures) {
        List groupableChannels = new LinkedList();
        HashMap bandGain = groupByBandGain(channels);
        Iterator iter = bandGain.keySet().iterator();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            LinkedList chn = (LinkedList)bandGain.get(key);
            LinkedList failedList = new LinkedList();
            List channelList = getGroupableChannels(chn, rules, failedList);
            failures.addAll(failedList);
            if(channelList.size() > 0) {
                groupableChannels.addAll(channelList);
            }
        }
        ChannelGroup[] channelGroups = new ChannelGroup[groupableChannels.size()];
        for(int k = 0; k < groupableChannels.size(); k++) {
            channelGroups[k] = new ChannelGroup((Channel[])((List)(groupableChannels.get(k))).toArray(new Channel[0]));
        }
        return (channelGroups);
    }

    private List getGroupableChannels(LinkedList chn,
                                      Element[] rules,
                                      List failedList) {
        LinkedList groupableChannels = new LinkedList();
        try {
            Channel[] channels = (Channel[])chn.toArray(new Channel[0]);
            HashMap channelMap = new HashMap();
            for(int chnCnt = 0; chnCnt < channels.length; chnCnt++) {
                String key = ChannelIdUtil.toStringNoDates(channels[chnCnt].get_id());
                key = key.substring(key.length() - 1, key.length());
                channelMap.put(key, channels[chnCnt]);
            }
            if(rules != null) {
                for(int ruleCnt = 0; ruleCnt < rules.length; ruleCnt++) {
                    NodeList children = rules[ruleCnt].getChildNodes();
                    boolean accept = true;
                    for(int i = 0; i < children.getLength(); i++) {
                        Node node = children.item(i);
                        if(accept) {
                            if(node instanceof Element) {
                                Element el = (Element)node;
                                if(el.getTagName().equals("threeCharacterRule")) {
                                    String orientationCodes = SodUtil.getNestedText(el);
                                    char[] codes = orientationCodes.trim()
                                            .toCharArray();
                                    LinkedList channelGroup = new LinkedList();
                                    for(int codeCount = 0; codeCount < codes.length; codeCount++) {
                                        if(channelMap.get("" + codes[codeCount]) != null) {
                                            channelGroup.add(channelMap.get(""
                                                    + codes[codeCount]));
                                        }
                                    }
                                    if(channelGroup.size() == 3) {
                                        Channel[] successfulChannels = (Channel[])channelGroup.toArray(new Channel[0]);
                                        if(sanityCheck(successfulChannels)) {
                                            groupableChannels.add(channelGroup);
                                            // remove the channels that are
                                            // successfully grouped
                                            for(int count = 0; count < successfulChannels.length; count++) {
                                                chn.remove(successfulChannels[count]);
                                                channelMap.remove(successfulChannels[count]);
                                            }
                                        }
                                    }
                                } else {
                                    Object subsetter = SodUtil.load(el,
                                                                    NetworkArm.PACKAGES);
                                    if(subsetter instanceof NetworkSubsetter) {
                                        NetworkId netId = channels[0].get_id().network_id;
                                        CacheNetworkAccess[] networks = Start.getNetworkArm()
                                                .getSuccessfulNetworks();
                                        NetworkAttr netAttr = null;
                                        for(int nCount = 0; nCount < networks.length; nCount++) {
                                            if(NetworkIdUtil.areEqual(networks[nCount]
                                                                              .get_attributes()
                                                                              .get_id(),
                                                                      netId)) {
                                                netAttr = networks[nCount]
                                                        .get_attributes();
                                            }
                                            NetworkSubsetter netSubsetter = (NetworkSubsetter)subsetter;
                                            if(!netSubsetter.accept(netAttr).isSuccess()) {
                                                accept = false;
                                            }
                                        }
                                    } else if(subsetter instanceof StationSubsetter) {
                                        if(accept) {
                                            StationSubsetter stationSubsetter = (StationSubsetter)subsetter;
                                            NetworkAccess network = Start.getNetworkArm()
                                                    .getNetwork(channels[0].my_site.my_station.get_id().network_id);
                                            if(!stationSubsetter.accept(channels[0].my_site.my_station,
                                                                        network).isSuccess()) {
                                                accept = false;
                                            }
                                        }
                                    } else if(subsetter instanceof ChannelSubsetter) {
                                        if(accept) {
                                            ChannelSubsetter channelSubsetter = (ChannelSubsetter)subsetter;
                                            for(int count = 0; count < channels.length; count++) {
                                                ProxyNetworkAccess network = Start.getNetworkArm()
                                                        .getNetwork(channels[count].get_id().network_id);
                                                if(!channelSubsetter.accept(channels[count],
                                                                            network).isSuccess()) {
                                                    accept = false;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(chn.size() > 0) {
                    failedList.addAll(chn);
                }
            }
        } catch(ConfigurationException e) {
            GlobalExceptionHandler.handle("Error while loading Sod element in grouper",
                                          e);
        } catch(Exception e) {
            GlobalExceptionHandler.handle("Exception while grouping channels",
                                          e);
        }
        return groupableChannels;
    }

    public static boolean sanityCheck(Channel[] channelGroup) {
        return haveSameSamplingRate(channelGroup)
                && areOrthogonal(channelGroup);
    }

    private static boolean areOrthogonal(Channel[] channelGroup) {
        for(int i = 0; i < channelGroup.length; i++) {
            for(int j = i + 1; j < channelGroup.length; j++) {
                if(!OrientationUtil.areOrthogonal(channelGroup[i].an_orientation,
                                                  channelGroup[j].an_orientation)) {
                    logger.info("Fail areOrthogonal ("+i+","+j+"): "+ChannelIdUtil.toString(channelGroup[i].get_id())+" "+OrientationUtil.toString(channelGroup[i].an_orientation)+" "+ChannelIdUtil.toString(channelGroup[j].get_id())+" "+OrientationUtil.toString(channelGroup[j].an_orientation));
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean haveSameSamplingRate(Channel[] channelGroup) {
        SamplingImpl sampl = (SamplingImpl)channelGroup[0].sampling_info;
        QuantityImpl freq = sampl.getFrequency();
        UnitImpl baseUnit = freq.getUnit();
        double samplingRate0 = (freq.getValue()) * sampl.getNumPoints();
        for(int i = 1; i < channelGroup.length; i++) {
            SamplingImpl sample = (SamplingImpl)channelGroup[i].sampling_info;
            double sampleRate = (sample.getFrequency().convertTo(baseUnit).getValue())
                    * sample.getNumPoints();
            if(sampleRate != samplingRate0) {
                logger.info("Fail haveSameSamplingRate ("+i+"): "+ChannelIdUtil.toString(channelGroup[i].get_id())+" "+channelGroup[i].sampling_info+" "+ChannelIdUtil.toString(channelGroup[0].get_id())+" "+channelGroup[0].sampling_info);
                return false;
            }
        }
        return true;
    }

    private HashMap groupByBandGain(Channel[] channels) {
        HashMap bandGain = new HashMap();
        for(int i = 0; i < channels.length; i++) {
            MicroSecondDate msd = new MicroSecondDate(channels[i].get_id().begin_time);
            String key = ChannelIdUtil.toStringNoDates(channels[i].get_id());
            key = key.substring(0, key.length() - 1);
            key = msd + key;
            LinkedList chans = (LinkedList)bandGain.get(key);
            if(chans == null) {
                chans = new LinkedList();
                bandGain.put(key, chans);
            }
            chans.add(channels[i]);
        }
        return bandGain;
    }

    // If the config file in invalid the validator throws a SAXException
    private Element[] loadRules(String configFileLoc) {
        if(configFileLoc == null) {
            return new Element[0];
        }
        Validator validator = new Validator(grouperSchemaLoc);
        Element[] rules = null;
        try {
            if(!validator.validate(getRules(configFileLoc))) {
                logger.info("Invalid config file!");
            } else {
                Document doc = Start.createDoc(getRules(configFileLoc),
                                               configFileLoc);
                NodeList ruleList = doc.getElementsByTagName("rule");
                rules = new Element[ruleList.getLength()];
                for(int i = 0; i < ruleList.getLength(); i++) {
                    rules[i] = (Element)ruleList.item(i);
                }
            }
        } catch(ParserConfigurationException e) {
            GlobalExceptionHandler.handle("Invalid config File "
                    + configFileLoc + " for Channel Grouper", e);
            return new Element[0];
        } catch(SAXException e) {
            GlobalExceptionHandler.handle("Invalid config File "
                    + configFileLoc + " for Channel Grouper", e);
            return new Element[0];
        } catch(IOException e) {
            GlobalExceptionHandler.handle("Config File for Channel Grouper "
                    + configFileLoc + " not found ", e);
            return new Element[0];
        }
        return rules;
    }

    private InputSource getRules(String configFileLoc) throws IOException {
        ClassLoader loader = ChannelGrouper.class.getClassLoader();
        return Start.createInputSource(loader, configFileLoc);
    }

    private static Logger logger = Logger.getLogger(ChannelGrouper.class);

    private Element[] defaultRules;

    private Element[] additionalRules;

    private static String defaultConfigFileLoc = "jar:edu/sc/seis/sod/data/grouper.xml";

    private static final String grouperSchemaLoc = "edu/sc/seis/sod/data/grouper.rng";
}
