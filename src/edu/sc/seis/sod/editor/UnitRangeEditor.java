/**
 * UnitRangeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;



public class UnitRangeEditor implements EditorPlugin {
    public UnitRangeEditor(UnitImpl[] units){
        this.units = units;
    }

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        Text minText = (Text)XPathAPI.selectSingleNode(element, "min/text()");
        Text maxText = (Text)XPathAPI.selectSingleNode(element, "max/text()");
        JSpinner minSpinner = EditorUtil.createNumberSpinner(minText, 0, 1000, 100);
        JSpinner maxSpinner = EditorUtil.createNumberSpinner(maxText, 0, 1000, 100);
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("From: "));
        b.add(minSpinner);
        b.add(new JLabel("  to  "));
        b.add(maxSpinner);
        b.add(new JLabel("  in  "));
        if(units.length > 1) {
            JComboBox box = new JComboBox(units);
            b.add(box);
        }else{
            JLabel unitLabel = new JLabel(UnitDisplayUtil.getNameForUnit(units[0]));
            b.add(unitLabel);
        }
        b.add(Box.createHorizontalGlue());
        return b;
    }

    private UnitImpl[] units;
}

