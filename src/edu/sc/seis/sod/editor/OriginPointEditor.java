/**
 * OriginPointEditor.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.SodUtil;

public abstract class OriginPointEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws Exception {
        JComponent unitEditor = degreeEditor.getGUI(element);
        unitEditor.setBorder(new TitledBorder(getDegreeEditorName()));
        Box vBox = Box.createVerticalBox();
        vBox.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        vBox.add(createLatLonBox(element));
        vBox.add(unitEditor);
        return vBox;
    }

    public static Box createLatLonBox(Element element)
            throws TransformerException {
        Box latLonBox = Box.createHorizontalBox();
        latLonBox.setBorder(new TitledBorder("Center point"));
        latLonBox.add(Box.createHorizontalGlue());
        Element latEl = SodUtil.getElement(element, "latitude");
        Text latitude = (Text)XPathAPI.selectSingleNode(element,
                                                        "latitude/text()");
        latLonBox.add(new JLabel(SodGUIEditor.getDisplayName(latEl.getTagName())));
        latLonBox.add(EditorUtil.createNumberSpinner(latitude, -90, 90, 1));
        latLonBox.add(Box.createHorizontalStrut(10));
        Element lonEl = SodUtil.getElement(element, "longitude");
        Text longitude = (Text)XPathAPI.selectSingleNode(element,
                                                         "longitude/text()");
        latLonBox.add(new JLabel(SodGUIEditor.getDisplayName(lonEl.getTagName())));
        latLonBox.add(EditorUtil.createNumberSpinner(longitude, -180, 180, 1));
        latLonBox.add(Box.createHorizontalGlue());
        return latLonBox;
    }

    protected abstract String getDegreeEditorName();

    private UnitRangeEditor degreeEditor = new UnitRangeEditor(new UnitImpl[] {UnitImpl.DEGREE});
}