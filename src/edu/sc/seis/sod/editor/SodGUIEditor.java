/**
 * SodGUIEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Start;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.log4j.BasicConfigurator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;



public class SodGUIEditor extends SimpleGUIEditor {

    SodGUIEditor(String[] args) throws IOException, ParserConfigurationException, TransformerException, DOMException, SAXException, Exception {
        super(args);
        grammer = new SchemaGrammer();

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


    public void start() {
        super.start();
    }

    public SchemaGrammer getGrammer() {
        return grammer;
    }

    SchemaGrammer grammer;

    JComponent getCompForElement(Element element) {
        try {
            if (editors.containsKey(element.getTagName())) {
                return ((EditorPlugin)editors.get(element.getTagName())).getGUI(element);
            } else {
                return super.getCompForElement(element);
            }
        } catch (Exception e) {
            System.err.println("Can't get component for "+element.getTagName()+", using default. "+e);
            e.printStackTrace();
            return super.getCompForElement(element);
        }
    }

    protected void initEditors() {
        editors.put("property", new PropertyEditor());
        //editors.put("networkArm", new NetworkArmEditor(this));
        DateEditor dateEdit = new DateEditor();
        editors.put("startTime", dateEdit);
        editors.put("endTime", dateEdit);
        TimeRangeEditor timeRangeEdit = new TimeRangeEditor();
        editors.put("effectiveTimeOverlap", timeRangeEdit);
        NetCodeEditor netCodeEdit = new NetCodeEditor();
        editors.put("networkCode", netCodeEdit);
        editors.put("stationCode", netCodeEdit);
        editors.put("siteCode", netCodeEdit);
        editors.put("channelCode", netCodeEdit);
        editors.put("bandCode", netCodeEdit);
        editors.put("gainCode", netCodeEdit);
        editors.put("orientationCode", netCodeEdit);
        editors.put("boxArea", new BoxAreaEditor());
        editors.put("distanceRange", new DistanceRangeEditor());
        editors.put("phaseRequest", new PhaseRequestEditor());
        editors.put("sacFileProcessor", new SacFileEditor(this));

        TagChooser originTC = new TagChooser("origin", this);
        Vector vector = originTC.getSubTypes();
        for (int i = 0; i < vector.size(); i++) {
            String tagName = (String)vector.get(i);
            editors.put(tagName, originTC);
        }

    }

    protected HashMap editors = new HashMap();

    protected Start start;

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        SodGUIEditor gui = new SodGUIEditor(args);
        gui.start();
    }

}


