/**
 * UnitRangeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.display.UnitDisplayUtil;



public class UnitRangeEditor implements EditorPlugin {
    public UnitRangeEditor(UnitImpl[] units){
        this(units, false);
    }

    public UnitRangeEditor(UnitImpl[] units, boolean isBordered){
        this(units, 0, 1000, 100, isBordered);
    }

    public UnitRangeEditor(UnitImpl[] units, double min, double max, double step, boolean isBordered){
        this.units = units;
        this.min = min;
        this.max = max;
        this.step = step;
        this.isBordered = isBordered;
    }

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createHorizontalBox();
        Text minText = (Text)XPathAPI.selectSingleNode(element, "min/text()");
        Text maxText = (Text)XPathAPI.selectSingleNode(element, "max/text()");
        JSpinner minSpinner = EditorUtil.createNumberSpinner(minText, min, max, step);
        JSpinner maxSpinner = EditorUtil.createNumberSpinner(maxText, min, max, step);
        b.add(Box.createHorizontalGlue());
        if(!isBordered){
            b.add(new JLabel(SimpleGUIEditor.getDisplayName(element.getTagName())));
            b.add(Box.createHorizontalStrut(10));
        }
        b.add(new JLabel("From: "));
        b.add(minSpinner);
        b.add(new JLabel("  to  "));
        b.add(maxSpinner);
        if(units.length > 1) {
            JComboBox box = new JComboBox(units);
            b.add(box);
        }else{
            JLabel unitLabel = new JLabel(UnitDisplayUtil.getNameForUnit(units[0]));
            b.add(Box.createHorizontalStrut(10));
            b.add(unitLabel);
        }
        b.add(Box.createHorizontalGlue());
        if (isBordered){
            b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        }
        return b;
    }

    private UnitImpl[] units;
    private double min, max, step;
    private boolean isBordered;
}


