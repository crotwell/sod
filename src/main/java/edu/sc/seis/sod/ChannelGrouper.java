/**
 * ChannelGrouper.java
 * 
 * @author Jagadeesh Danala
 * @version
 */
package edu.sc.seis.sod;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.bag.OrientationUtil;
import edu.sc.seis.sod.channelGroup.Rule;
import edu.sc.seis.sod.model.common.Orientation;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.validator.Validator;

public class ChannelGrouper {

    public ChannelGrouper() throws ConfigurationException {
        this(null);
    }

    public ChannelGrouper(String configFileLoc) throws ConfigurationException {
        try {
            defaultRules = loadRules(defaultConfigFileLoc);
            additionalRules = loadRules(configFileLoc);
        } catch(IOException e) {
            throw new ConfigurationException("Unable to configure three component rules", e);
        } catch(SAXException e) {
            throw new ConfigurationException("Unable to configure three component rules", e);
        } catch(ParserConfigurationException e) {
            throw new ConfigurationException("Unable to configure three component rules", e);
        }
    }

    /** group channels into three components of motion. It is assumed that all the channels
     * in the list are from the same network.station.
     * @param channels
     * @param failures
     * @return
     */
    public List<ChannelGroup> group(List<Channel> channels, List<Channel> failures) {
        return applyRules(channels, defaultRules, additionalRules, failures);
    }

    private List<ChannelGroup> applyRules(List<Channel> channels,
                                          List<Rule> defaultRules,
                                          List<Rule> additionalRules,
                                          List<Channel> failures) {
        List<ChannelGroup> groupableChannels = new LinkedList<ChannelGroup>();
        HashMap<String, List<Channel>> bandGain = groupByNetStaBandGain(channels);
        Iterator<String> iter = bandGain.keySet().iterator();
        while(iter.hasNext()) {
            String key = iter.next();
            List<Channel> toTest = new ArrayList<Channel>();
            toTest.addAll(bandGain.get(key));
            for (Rule rule : additionalRules) {
                List<Channel> stillToTest = new ArrayList<Channel>();
                List<ChannelGroup> groups = rule.acceptable(toTest, stillToTest);
                groupableChannels.addAll(groups);
                toTest = stillToTest;
            }
            for (Rule rule : defaultRules) {
                List<Channel> stillToTest = new ArrayList<Channel>();
                List<ChannelGroup> groups = rule.acceptable(toTest, stillToTest);
                groupableChannels.addAll(groups);
                toTest = stillToTest;
            }
            failures.addAll(toTest);
        }
        return groupableChannels;
    }


    public static boolean sanityCheck(ChannelGroup channelGroup) {
        return haveSameSamplingRate(channelGroup)
                && areOrthogonal(channelGroup);
    }

    private static boolean areOrthogonal(ChannelGroup channelGroup) {
        Channel[] chans = channelGroup.getChannels();
        for(int i = 0; i < chans.length; i++) {
            for(int j = i + 1; j < chans.length; j++) {
                if(!OrientationUtil.areOrthogonal(chans[i],
                                                  chans[j])) {
                    logger.info("Fail areOrthogonal ("+i+","+j+"): "+ChannelIdUtil.toString(chans[i])+" "+OrientationUtil.toString(Orientation.of(chans[i]))+" "+ChannelIdUtil.toString(chans[j])+" "+OrientationUtil.toString( Orientation.of(chans[j])));
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean haveSameSamplingRate(ChannelGroup cg) {
        Channel[] chans = cg.getChannels();
        SamplingImpl sampl = SamplingImpl.of(chans[0]);
        QuantityImpl freq = sampl.getFrequency();
        UnitImpl baseUnit = freq.getUnit();
        double samplingRate0 = (freq.getValue()) * sampl.getNumPoints();
        for(int i = 1; i < chans.length; i++) {
            SamplingImpl sample = SamplingImpl.of(chans[i]);
            double sampleRate = (sample.getFrequency().convertTo(baseUnit).getValue())
                    * sample.getNumPoints();
            if(sampleRate != samplingRate0) {
                logger.info("Fail haveSameSamplingRate ("+i+"): "+ChannelIdUtil.toString(chans[i])+" "+SamplingImpl.of(chans[i])+" "+ChannelIdUtil.toString(chans[0])+" "+SamplingImpl.of(chans[0]));
                return false;
            }
        }
        return true;
    }

     HashMap<String, List<Channel>> groupByNetStaBandGain(List<Channel> channels) {
        HashMap<String, List<Channel>> bandGain = new HashMap<String, List<Channel>>();
        for(Channel c : channels) {
            Instant msd = c.getStartDateTime));
            String key = c.getNetworkId()+"."+c.getStationCode()+"."+c.getCode();
            key = key.substring(0, key.length() - 1);
            key = msd + key;
            List<Channel> chans = bandGain.get(key);
            if(chans == null) {
                chans = new LinkedList<Channel>();
                bandGain.put(key, chans);
            }
            chans.add(c);
        }
        return bandGain;
    }

    // If the config file in invalid the validator throws a SAXException
    private List<Rule> loadRules(String configFileLoc) throws IOException, SAXException, ParserConfigurationException, ConfigurationException {
        List<Rule> out = new ArrayList<Rule>();
        if(configFileLoc != null && configFileLoc.length() != 0 ) {
            Validator validator = new Validator(grouperSchemaLoc);
            if(!validator.validate(getRules(configFileLoc))) {
                throw new ConfigurationException("Invalid config file! "+configFileLoc+" "+validator.getErrorMessage());
            } else {
                Document doc = Start.createDoc(getRules(configFileLoc),
                                               configFileLoc);
                NodeList ruleList = doc.getElementsByTagName("rule");
                for(int i = 0; i < ruleList.getLength(); i++) {
                    out.add(new Rule((Element)ruleList.item(i), configFileLoc+" "+i));
                }
            }
        }
        return out;
    }

    private InputSource getRules(String configFileLoc) throws IOException {
        ClassLoader loader = ChannelGrouper.class.getClassLoader();
        return Start.createInputSource(loader, configFileLoc);
    }

    private static Logger logger = LoggerFactory.getLogger(ChannelGrouper.class);

    private List<Rule> defaultRules;

    private List<Rule> additionalRules;
    
    List<Rule> ruleList = new ArrayList<Rule>();

    private static String defaultConfigFileLoc = "jar:edu/sc/seis/sod/data/grouper.xml";

    private static final String grouperSchemaLoc = "edu/sc/seis/sod/data/grouper.rng";
}
