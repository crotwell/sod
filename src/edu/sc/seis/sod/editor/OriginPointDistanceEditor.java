/**
 * PointDistanceEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.sc.seis.fissuresUtil.xml.XMLUtil;
import edu.sc.seis.sod.SodUtil;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class OriginPointDistanceEditor  implements EditorPlugin {
    public OriginPointDistanceEditor(SodGUIEditor editor) {
        this.editor = editor;
    }

    public JComponent getGUI(Element element) throws Exception {


        Box latLonBox = Box.createHorizontalBox();
        latLonBox.setBorder(new TitledBorder("Center point"));
        latLonBox.add(Box.createHorizontalGlue());

        Element latEl = SodUtil.getElement(element, "latitude");
        Text latitude = (Text)XPathAPI.selectSingleNode(element, "latitude/text()");
        latLonBox.add(new JLabel(SodGUIEditor.getDisplayName(latEl.getTagName())));
        latLonBox.add(EditorUtil.createNumberSpinner(latitude, -90, 90, 1));
        latLonBox.add(Box.createHorizontalStrut(10));

        Element lonEl = SodUtil.getElement(element, "longitude");
        Text longitude = (Text)XPathAPI.selectSingleNode(element, "longitude/text()");
        latLonBox.add(new JLabel(SodGUIEditor.getDisplayName(lonEl.getTagName())));
        latLonBox.add(EditorUtil.createNumberSpinner(longitude, -90, 90, 1));
        latLonBox.add(Box.createHorizontalGlue());

        EditorPlugin unitRangeEditor = editor.getCustomEditor("unitRange");
        JComponent unitEditor = unitRangeEditor.getGUI(element);
        unitEditor.setBorder(new TitledBorder("Distance from center point"));

        Box vBox = Box.createVerticalBox();
        vBox.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        vBox.add(latLonBox);
        vBox.add(unitEditor);
        return vBox;
    }

    SodGUIEditor editor;
}

