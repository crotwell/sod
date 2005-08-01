/**
 * SaveSeismogramToFileEditor.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.process.waveform.SaveSeismogramToFile;

public class SaveSeismogramToFileEditor extends PrintlineEventEditor {

    public SaveSeismogramToFileEditor() {}

    protected String getTitle() {
        return "Event Directory";
    }

    protected String getDefaultTemplateValue() {
        return SaveSeismogramToFile.DEFAULT_TEMPLATE;
    }

    protected String evaluate(String template) {
        return super.evaluate(template);
    }

    public JComponent getGUI(Element element) throws Exception {
        Box vBox = Box.createVerticalBox();
        vBox.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getNodeName())));
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("File Type"));
        b.add(Box.createHorizontalStrut(10));
        final Text text = getTextChildFromPossiblyNonexistentElement(element,
                                                                     "fileType",
                                                                     fileTypes[1]);
        final JComboBox fileTypeBox = new JComboBox(fileTypes);
        fileTypeBox.setSelectedItem(text.getNodeValue());
        fileTypeBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                text.setNodeValue(fileTypeBox.getSelectedItem().toString());
            }
        });
        b.add(fileTypeBox);
        b.add(Box.createHorizontalGlue());
        vBox.add(b);
                
        if (text.getNodeValue().equals("sac")) {
            if(DOMHelper.hasElement(element, "sacHeader")) {
                Element sacHeader = DOMHelper.extractElement(element,
                                                             "sacHeader");
                NodeList nl = DOMHelper.getElements(sacHeader, "phaseTime");
                for(int i = 0; i < nl.getLength(); i++) {
                    Element phaseEl = (Element)nl.item(i);
                    Box hBox = Box.createHorizontalBox();
                    hBox.setBorder(new TitledBorder("Phase Time"));
                    hBox.add(Box.createHorizontalStrut(10));
                    hBox.add(EditorUtil.getLabeledTextField(DOMHelper.getElement(phaseEl, "model"), "Model"));
                    hBox.add(Box.createHorizontalStrut(10));
                    hBox.add(EditorUtil.getLabeledTextField(DOMHelper.getElement(phaseEl, "phaseName"), "Phase Name"));
                    hBox.add(Box.createHorizontalStrut(10));
                    hBox.add(new JLabel("tHeader:"));
                    hBox.add(EditorUtil.createNumberSpinner(getTextChildFromPossiblyNonexistentElement(phaseEl, "tHeader", "0"), 0, 9, 1));
                    hBox.add(Box.createHorizontalGlue());
                    vBox.add(hBox);
                }
            }
        }
        
        b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("Data Directory"));
        b.add(Box.createHorizontalStrut(10));
        Text text2 = getTextChildFromPossiblyNonexistentElement(element,
                                                                "dataDirectory",
                                                                SaveSeismogramToFile.DEFAULT_DATA_DIRECTORY);
        text2 = (Text)XPathAPI.selectSingleNode(element, "dataDirectory/text()");
        b.add(EditorUtil.getTextField(text2));
        b.add(Box.createHorizontalGlue());
        vBox.add(b);
        b = Box.createHorizontalBox();
        b.setBorder(new TitledBorder(getTitle()));
        JComponent comp = createVelocityFilenameEditor(element,
                                                       getDefaultTemplateValue(),
                                                       "eventDirLabel",
                                                       "Label",
                                                       false);
        b.add(comp);
        vBox.add(b);
        return vBox;
    }

    public static String[] fileTypes = {"sac", "mseed"};
}
