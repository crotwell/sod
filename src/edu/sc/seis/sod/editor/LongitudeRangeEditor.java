/**
 * LongitudeRangeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.iris.Fissures.model.UnitImpl;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.w3c.dom.Element;

public class LongitudeRangeEditor extends UnitRangeEditor {

    public LongitudeRangeEditor(){
        super(new UnitImpl[]{UnitImpl.DEGREE}, -180, 180, 5);
    }

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("Longitude"));
        b.add(super.getGUI(element));
        b.add(Box.createHorizontalGlue());
        return b;
    }

}

