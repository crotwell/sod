/**
 * PhaseRequestEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.iris.Fissures.model.UnitImpl;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class PhaseRequestEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        Box vertBox = Box.createVerticalBox();

        Box modelBox = Box.createHorizontalBox();
        String modelTagName = "model";
        Element modelElement = (Element)XPathAPI.selectSingleNode(element, modelTagName);
        if (modelElement != null) {
            modelBox.add(EditorUtil.getLabel(SimpleGUIEditor.getDisplayName(modelTagName)));
            modelBox.add(EditorUtil.getComboBox(modelElement, MODEL_NAMES));
            modelBox.add(Box.createHorizontalGlue());
            vertBox.add(modelBox);
        }

        Box phaseBox = Box.createHorizontalBox();
        phaseBox.add(makePhaseEditor(element));
        phaseBox.add(Box.createHorizontalGlue());

        vertBox.add(phaseBox);

        vertBox.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        return vertBox;
    }

    private JComponent makePhaseEditor(Element element) throws TransformerException{
        JPanel phaseOffsetEditor = new JPanel();
        phaseOffsetEditor.setLayout(new GridLayout(3,5));
        phaseOffsetEditor.add(new JLabel(""));
        phaseOffsetEditor.add(new JLabel("Phase"));
        phaseOffsetEditor.add(new JLabel("Offset"));
        phaseOffsetEditor.add(new JLabel(""));
        phaseOffsetEditor.add(new JLabel(""));

        phaseOffsetEditor.add(EditorUtil.getLabel("Begin"));
        Node node = XPathAPI.selectSingleNode(element, "beginPhase/text()");
        Text text = (Text)node;
        phaseOffsetEditor.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "beginOffset/value/text()");
        text = (Text)node;
        phaseOffsetEditor.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "beginOffset/unit");
        phaseOffsetEditor.add(EditorUtil.getComboBox((Element)node, TIME_UNITS));
        phaseOffsetEditor.add(Box.createGlue());

        phaseOffsetEditor.add(EditorUtil.getLabel("End"));
        node = XPathAPI.selectSingleNode(element, "endPhase/text()");
        text = (Text)node;
        phaseOffsetEditor.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "endOffset/value/text()");
        text = (Text)node;
        phaseOffsetEditor.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "endOffset/unit");
        phaseOffsetEditor.add(EditorUtil.getComboBox((Element)node, TIME_UNITS));
        return phaseOffsetEditor;
    }

    public static final String[] MODEL_NAMES = { "iasp91", "prem" };
    public static final UnitImpl[] TIME_UNITS = {
        UnitImpl.SECOND,
            UnitImpl.MINUTE,
            UnitImpl.HOUR
    };

}
