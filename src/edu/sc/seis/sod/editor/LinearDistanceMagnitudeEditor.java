/**
 * LinearDistanceMagnitudeEditor.java
 *
 * @author Philip Crotwell
 */

package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import edu.iris.Fissures.model.UnitImpl;

public class LinearDistanceMagnitudeEditor extends UnitRangeEditor {

    public LinearDistanceMagnitudeEditor(SodGUIEditor editor, UnitImpl[] units){
        super(units);
        this.editor = editor;
    }

    public LinearDistanceMagnitudeEditor(SodGUIEditor editor, UnitImpl[] units, boolean isBordered){
        super(units, isBordered);
        this.editor = editor;
    }

    public LinearDistanceMagnitudeEditor(SodGUIEditor editor, UnitImpl[] units, double min, double max, double step, boolean isBordered){
        super(units,  min,  max,  step,  isBordered);
        this.editor = editor;
    }


    public JComponent getGUI(Element element) throws Exception {
        Box b = (Box)super.getGUI(element);
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName("distanceRange")));
        Box out = Box.createVerticalBox();
        Element child = (Element)XPathAPI.selectSingleNode(element, "magnitudeRange");
        out.add(magEdit.getGUI(child));
        out.add(b);
        out.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        return out;
    }

    private SodGUIEditor editor;

    private MagnitudeEditor magEdit = new MagnitudeEditor();
}

