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
        Box box = Box.createHorizontalBox();
        box.add(new JLabel("Start:"));
        Node node = XPathAPI.selectSingleNode(element, "min");
        if(node == null){
            node = XPathAPI.selectSingleNode(element, "startTime");
        }
        Element minElement = (Element)node;
        box.add(Box.createHorizontalGlue());
        box.add(dateE.getGUI(minElement));
        box.add(new JLabel("End:"));
        node = XPathAPI.selectSingleNode(element, "max");
        if(node == null){
            node = XPathAPI.selectSingleNode(element, "endTime");
        }
        Element maxElement = (Element)node;
        box.add(dateE.getGUI(maxElement));
        box.add(Box.createHorizontalGlue());
        return box;
    }

    private DateEditor dateE = new DateEditor();
}

