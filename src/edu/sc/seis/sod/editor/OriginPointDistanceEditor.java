/**
 * PointDistanceEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.SodUtil;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class OriginPointDistanceEditor  extends OriginPointEditor {

    protected String getDegreeEditorName() {
        return "Distance from center point";
    }


}

