/**
 * DistanceRangeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import javax.swing.JComboBox;
import edu.iris.Fissures.model.UnitImpl;

public class DistanceRangeEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,4));
        panel.add(new JLabel("Distance"));
        panel.add(new JLabel("Min"));
        panel.add(new JLabel("Max"));
        panel.add(new JLabel("Unit"));

        panel.add(new JLabel(""));
        Node node = XPathAPI.selectSingleNode(element, "min/text()");
        Text text = (Text)node;
        panel.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "max/text()");
        text = (Text)node;
        panel.add(EditorUtil.getTextField(text));


        node = XPathAPI.selectSingleNode(element, "unit/text()");
        text = (Text)node;
        JComboBox combo = new JComboBox(DISTANCE_UNITS);
        combo.addItem(text.getNodeValue());
        combo.setSelectedItem(text.getNodeValue());
        combo.addItemListener(new TextItemListener(text));
        panel.add(combo);
        return panel;
    }

    static UnitImpl[] DISTANCE_UNITS = {
        UnitImpl.DEGREE,
            UnitImpl.KILOMETER,
            UnitImpl.METER
    };
}

