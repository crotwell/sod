/**
 * SodGUIEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UpdateChecker;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



public class SodGUIEditor extends SimpleGUIEditor {

    public SodGUIEditor(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException, Exception {
        super(args);
        UpdateChecker check = new UpdateChecker(true);
        frameName = "SOD Editor";
        setTabbed(true);
        initEditors();
    }

    JComponent getCompForElement(Element element) {
        try {
            String tag = element.getTagName();
            if (editors.containsKey(tag)){
                EditorPlugin ed = (EditorPlugin)editors.get(tag);
                return ed.getGUI(element);
            } else {
                return super.getCompForElement(element);
            }
        } catch (Exception e) {
            System.err.println("Can't get component for "+element.getTagName()+", using default. "+e);
            logger.warn("Can't get component for "+element.getTagName()+", using default. ",e);
            return super.getCompForElement(element);
        }
    }

    /** gets a custom added editor for the tagName, null if there isn't one*/
    public EditorPlugin getCustomEditor(String tagName) {
        return (EditorPlugin)editors.get(tagName);
    }

    protected void initEditors() {
        //editors.put("networkArm", new NetworkArmEditor(this));
        DateEditor dateEdit = new DateEditor();
        editors.put("startTime", dateEdit);
        editors.put("endTime", dateEdit);
        TimeRangeEditor timeRangeEdit = new TimeRangeEditor();
        editors.put("effectiveTimeOverlap", timeRangeEdit);
        editors.put("originTimeRange", new OriginTimeRangeEditor());
        NetCodeEditor netCodeEdit = new NetCodeEditor();
        editors.put("networkCode", netCodeEdit);
        editors.put("stationCode", netCodeEdit);
        editors.put("siteCode", netCodeEdit);
        editors.put("channelCode", netCodeEdit);
        editors.put("bandCode", netCodeEdit);
        editors.put("gainCode", netCodeEdit);
        editors.put("orientationCode", netCodeEdit);
        editors.put("boxArea", new BoxAreaEditor(this));

        editors.put("stationEffectiveTimeOverlap", timeRangeEdit);
        editors.put("siteEffectiveTimeOverlap", timeRangeEdit);
        editors.put("channelEffectiveTimeOverlap", timeRangeEdit);

        editors.put("magnitudeRange", new MagnitudeEditor());
        editors.put("distanceRange", new UnitRangeEditor(ANGLE_UNITS, 0, 180, 5, true));
        editors.put("phaseRequest", new PhaseRequestEditor());
        editors.put("sacFileProcessor", new SacFileEditor(this));
        editors.put("originPointDistance", new OriginPointDistanceEditor());
        editors.put("originPointAzimuth", new OriginPointAzimuthEditor());
        editors.put("originPointBackAzimuth", new OriginPointBackAzimuthEditor());
        editors.put("seismicRegion", new SeismicRegionEditor());
        editors.put("geographicRegion", new GeographicRegionEditor());
        editors.put("eventStatusTemplate", new EventStatusTemplateEditor());
        editors.put("eventFinder", new EventFinderEditor(this));
        editors.put("networkFinder", new NetworkFinderEditor(this));
        editors.put("catalog", new CatalogEditor());
        editors.put("contributor", new ContributorEditor());
        editors.put("unitRange", new UnitRangeEditor(DISTANCE_UNITS));
        editors.put("originDepthRange", new UnitRangeEditor(DISTANCE_UNITS, true));
        editors.put("linearDistanceMagnitudeRange", new LinearDistanceMagnitudeEditor(this, DISTANCE_UNITS, true));
        editors.put("midPoint", new MidPointEditor(this));
        editors.put("latitudeRange", new  UnitRangeEditor(ANGLE_UNITS, -90, 90, 5, false));
        editors.put("longitudeRange", new UnitRangeEditor(ANGLE_UNITS, -180, 180, 5, false));
        editors.put("azimuthRange", new UnitRangeEditor(ANGLE_UNITS, 0, 360, 5, true));
        editors.put("backAzimuthRange", new UnitRangeEditor(ANGLE_UNITS, 0, 360, 5, true));
        editors.put("refreshInterval", new RefreshIntervalEditor());
        editors.put("saveSeismogramToFile", new SaveSeismogramToFileEditor());
        editors.put("responseWriter", new ResponseWriterEditor());
        editors.put("waveformNetworkStatus", new WaveformStatusEditor());
        editors.put("waveformStationStatus", new WaveformStatusEditor());

        SubelementEater se = new SubelementEater();
        editors.put("networkInfoTemplateGenerator", se);
        editors.put("waveformEventTemplateGenerator", se);
        editors.put("localSeismogramTemplateGenerator", se);

        editors.put("fixedDataCenter", new FixedDataCenterEditor(this));
        
        editors.put("legacyExecute", new LegacyExecuteEditor());
        editors.put("channelGroupLegacyExecute", new LegacyExecuteEditor());
        
        editors.put("breqFastRequestSubsetter", new BreqFastRequestSubsetterEditor());
        
        editors.put("phaseSignalToNoise", new PhaseSignalToNoiseEditor());

        BooleanEditor bool = new BooleanEditor(this);
        String[] switchTypes = { "origin", "network", "station", "site", "channel", "eventStation", "eventChannel", "availableData", "requestSubsetter", "seismogram"};
        String[] logicals = { "AND", "OR", "NOT" }; // what about XOR?
        for (int i = 0; i < switchTypes.length; i++) {
            for (int j = 0; j < logicals.length; j++) {
                editors.put(switchTypes[i]+logicals[j], bool);
            }
        }

        for (int i = 0; i < switchTypes.length; i++) {
            Switcher originSwitcher = new Switcher(switchTypes[i], this);
            List subTypes = originSwitcher.getSubTypes();
            Iterator it = subTypes.iterator();
            while(it.hasNext()) {
                String tagName = (String)it.next();
                if (editors.containsKey(tagName)) {
                    // save original with _tagChooser appended
                    editors.put(tagName+TagChooser.PLUGIN_SUFFIX, editors.get(tagName));
                }
                editors.put(tagName, originSwitcher);
            }
        }


        String[] wrapperTypes = { "AvailableDataWrapper", "RequestSubsetterWrapper", "LocalSeismogramWrapper", "EventChannelWrapper" };
        String[] wrapperLogicals = { "AND", "OR" };
        for (int i = 0; i < wrapperTypes.length; i++) {
            for (int j = 0; j < wrapperLogicals.length; j++) {
                editors.put(wrapperLogicals[j]+wrapperTypes[i], bool);
            }
        }
    }

    protected HashMap editors = new HashMap();

    public static final UnitImpl[] DISTANCE_UNITS = {  UnitImpl.KILOMETER };

    public static final UnitImpl[] ANGLE_UNITS = { UnitImpl.DEGREE };

    public static final UnitImpl[] TIME_UNITS = { UnitImpl.SECOND, UnitImpl.MINUTE,
            UnitImpl.HOUR, UnitImpl.DAY, UnitImpl.WEEK, };


    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        SodGUIEditor gui = new SodGUIEditor(args);
        gui.start();
    }

    private static final Logger logger = Logger.getLogger(SodGUIEditor.class);
}
