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
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class PhaseRequestEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {

        Box box = Box.createVerticalBox();
        Box topRow = Box.createHorizontalBox();
        box.add(topRow);
        Box modelRow = Box.createHorizontalBox();
        box.add(modelRow);
        Box botRowA = Box.createHorizontalBox();
        box.add(botRowA);
        Box botRowB = Box.createHorizontalBox();
        box.add(botRowB);

        topRow.add(new JLabel(EditorUtil.capFirstLetter(element.getTagName())));
        topRow.add(Box.createGlue());

        modelRow.add(Box.createRigidArea(new Dimension(10, 10)));
        modelRow.add(new JLabel("Model"));
        Element modelElement = (Element)XPathAPI.selectSingleNode(element, "model");
        modelRow.add(EditorUtil.getComboBox(modelElement, MODEL_NAMES));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,5));

        botRowA.add(Box.createRigidArea(new Dimension(10, 10)));
        botRowA.add(panel);

        panel.add(new JLabel(""));
        panel.add(new JLabel("Phase"));
        panel.add(new JLabel("Offset"));
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        panel.add(new JLabel("Begin"));
        Node node = XPathAPI.selectSingleNode(element, "beginPhase/text()");
        Text text = (Text)node;
        panel.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "beginOffset/value/text()");
        text = (Text)node;
        panel.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "beginOffset/unit");
        panel.add(EditorUtil.getComboBox((Element)node, TIME_UNITS));
        panel.add(Box.createGlue());

        panel.add(new JLabel("End"));
        node = XPathAPI.selectSingleNode(element, "endPhase/text()");
        text = (Text)node;
        panel.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "endOffset/value/text()");
        text = (Text)node;
        panel.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "endOffset/unit");
        panel.add(EditorUtil.getComboBox((Element)node, TIME_UNITS));
        panel.add(Box.createGlue());

        return box;
    }

    public static final String[] MODEL_NAMES = { "iasp91", "prem" };
    public static final UnitImpl[] TIME_UNITS = {
        UnitImpl.SECOND,
            UnitImpl.MINUTE,
            UnitImpl.HOUR
    };

}
