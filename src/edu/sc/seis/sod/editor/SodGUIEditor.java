/**
 * SodGUIEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.exceptionHandler.FilterReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.GUIReporter;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Start;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.omg.CORBA.COMM_FAILURE;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



public class SodGUIEditor extends SimpleGUIEditor {
    static {
        GlobalExceptionHandler.registerWithAWTThread();
    }

    public SodGUIEditor(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException, Exception {
        super(args);

        List ignoreList = new ArrayList();
        // silently eat CommFailure
        ignoreList.add(COMM_FAILURE.class);
        GlobalExceptionHandler.add(new FilterReporter(new GUIReporter(), ignoreList));

        grammar = new SchemaGrammar();
        frameName = "SOD Editor";
        tabs = true;
        initEditors();
        JPanel sodPanel = new JPanel();
        sodPanel.setName("Sod");
        getTabPane().add(sodPanel);
        sodPanel.setLayout(new BorderLayout());
        final JButton go = new JButton("GO!");
        sodPanel.add(go, BorderLayout.SOUTH);
        final TextAreaStatusDisplay statusDisp = new TextAreaStatusDisplay();
        sodPanel.add(new JScrollPane(statusDisp.getTextArea()), BorderLayout.CENTER);
        go.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        try {
                            go.setText("Going...");
                            start = new Start(getDocument());
                            start.start();

                            start.getEventArm().add(statusDisp);
                            start.getWaveformArm().addStatusMonitor(statusDisp);
                        } catch (Throwable t) {
                            GlobalExceptionHandler.handle("Problem starting SOD", t);
                            go.setText("Gone.  :(");
                        }
                    }
                });

    }

    public SchemaGrammar getGrammar() {  return grammar; }

    SchemaGrammar grammar;

    JComponent getCompForElement(Element element) {
        try {
            String tag = element.getTagName();
            if (editors.containsKey(tag)){
                return ((EditorPlugin)editors.get(tag)).getGUI(element);
            } else {
                return super.getCompForElement(element);
            }
        } catch (Exception e) {
            System.err.println("Can't get component for "+element.getTagName()+", using default. "+e);
            e.printStackTrace();
            return super.getCompForElement(element);
        }
    }

    /** gets a custom added editor for the tagName, null if there isn't one*/
    public EditorPlugin getCustomEditor(String tagName) {
        return (EditorPlugin)editors.get(tagName);
    }

    protected void initEditors() {
        editors.put("property", new PropertyEditor());
        //editors.put("networkArm", new NetworkArmEditor(this));
        DateEditor dateEdit = new DateEditor();
        editors.put("startTime", dateEdit);
        editors.put("endTime", dateEdit);
        TimeRangeEditor timeRangeEdit = new TimeRangeEditor();
        editors.put("effectiveTimeOverlap", timeRangeEdit);
        editors.put("eventTimeRange", new EventTimeRangeEditor());
        NetCodeEditor netCodeEdit = new NetCodeEditor();
        editors.put("networkCode", netCodeEdit);
        editors.put("stationCode", netCodeEdit);
        editors.put("siteCode", netCodeEdit);
        editors.put("channelCode", netCodeEdit);
        editors.put("bandCode", netCodeEdit);
        editors.put("gainCode", netCodeEdit);
        editors.put("orientationCode", netCodeEdit);
        editors.put("boxArea", new BoxAreaEditor(this));
        EffectiveTimeEditor effTime = new EffectiveTimeEditor();
        editors.put("stationEffectiveTimeOverlap", effTime);
        editors.put("siteEffectiveTimeOverlap", effTime);
        editors.put("channelEffectiveTimeOverlap", effTime);
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
        editors.put("midPoint", new MidPointEditor(this));
        editors.put("latitudeRange", new  UnitRangeEditor(ANGLE_UNITS, -90, 90, 5, false));
        editors.put("longitudeRange", new UnitRangeEditor(ANGLE_UNITS, -180, 180, 5, false));
        editors.put("azimuthRange", new UnitRangeEditor(ANGLE_UNITS, 0, 360, 5, true));
        editors.put("backAzimuthRange", new UnitRangeEditor(ANGLE_UNITS, 0, 360, 5, true));
        editors.put("networkInfoTemplateGenerator", new NetworkInfoTemplateGeneratorEditor());

        BooleanEditor bool = new BooleanEditor(this);
        String[] switchTypes = { "origin", "network", "station", "site", "channel", "eventStation", "eventChannel", "availableData", "seismogramSubsetter"};
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
    }

    protected HashMap editors = new HashMap();

    protected Start start;

    private static final UnitImpl[] DISTANCE_UNITS = {  UnitImpl.KILOMETER };

    private static final UnitImpl[] ANGLE_UNITS = { UnitImpl.DEGREE };

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        SodGUIEditor gui = new SodGUIEditor(args);
        gui.start();
    }
}

