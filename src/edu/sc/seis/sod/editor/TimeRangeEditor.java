/**
 * TimeRangeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class TimeRangeEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        DateEditor dateE = new DateEditor();
        Box box = Box.createVerticalBox();
        Box minRow = Box.createHorizontalBox();
        box.add(minRow);
        Box maxRow = Box.createHorizontalBox();
        box.add(maxRow);

        minRow.add(new JLabel("Min"));
        Node node = XPathAPI.selectSingleNode(element, "min");
        Element minElement = (Element)node;
        minRow.add(dateE.getGUI(minElement));
        minRow.add(Box.createHorizontalGlue());

        maxRow.add(new JLabel("Max"));
        node = XPathAPI.selectSingleNode(element, "min");
        Element maxElement = (Element)node;
        maxRow.add(dateE.getGUI(maxElement));
        maxRow.add(Box.createHorizontalGlue());
        return box;
    }
}

