/**
 * LatitudeRangeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.SodUtil;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.w3c.dom.Element;

public class LatitudeRangeEditor extends UnitRangeEditor{

    public LatitudeRangeEditor(){
        super(new UnitImpl[]{UnitImpl.DEGREE}, -90, 90, 5);
    }

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("Latitude"));
        b.add(super.getGUI(element));
        b.add(Box.createHorizontalGlue());
        return b;
    }

}

