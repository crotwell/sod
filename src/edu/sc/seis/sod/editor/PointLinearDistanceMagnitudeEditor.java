package edu.sc.seis.sod.editor;

import javax.swing.JComponent;
import org.w3c.dom.Element;
import edu.iris.Fissures.model.UnitImpl;

/**
 * @author oliverpa Created on May 23, 2005
 */
public class PointLinearDistanceMagnitudeEditor extends
        LinearDistanceMagnitudeEditor {

    public PointLinearDistanceMagnitudeEditor(SodGUIEditor editor,
                                              UnitImpl[] units) {
        super(editor, units);
    }

    public PointLinearDistanceMagnitudeEditor(SodGUIEditor editor,
                                              UnitImpl[] units,
                                              boolean isBordered) {
        super(editor, units, isBordered);
    }

    public PointLinearDistanceMagnitudeEditor(SodGUIEditor editor,
                                              UnitImpl[] units,
                                              double min,
                                              double max,
                                              double step,
                                              boolean isBordered) {
        super(editor, units, min, max, step, isBordered);
    }

    public JComponent getGUI(Element el) throws Exception {
        JComponent comp = super.getGUI(el);
        comp.add(OriginPointEditor.createLatLonBox(el));
        return comp;
    }
}