/**
 * BoxAreaEditor.java
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

public class BoxAreaEditor  implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3,3));
        panel.add(new JLabel("Area"));
        panel.add(new JLabel("Min"));
        panel.add(new JLabel("Max"));

        panel.add(new JLabel("Latitude"));
        Node node = XPathAPI.selectSingleNode(element, "latitudeRange/min/text()");
        Text text = (Text)node;
        panel.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "latitudeRange/max/text()");
        text = (Text)node;
        panel.add(EditorUtil.getTextField(text));

        panel.add(new JLabel("Longitude"));
        node = XPathAPI.selectSingleNode(element, "longitudeRange/min/text()");
        text = (Text)node;
        panel.add(EditorUtil.getTextField(text));
        node = XPathAPI.selectSingleNode(element, "longitudeRange/max/text()");
        text = (Text)node;
        panel.add(EditorUtil.getTextField(text));

        return panel;
    }
}

