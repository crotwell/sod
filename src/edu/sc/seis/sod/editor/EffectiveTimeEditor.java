/**
 * EffectiveTimeEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;



public class EffectiveTimeEditor implements EditorPlugin{
    public JComponent getGUI(Element el) throws Exception {
        Element effectiveTime = (Element)XPathAPI.selectSingleNode(el, "effectiveTimeOverlap");
        JComponent timeRangeEditor = tre.getGUI(effectiveTime);
        timeRangeEditor.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(el.getTagName())));
        return timeRangeEditor;
    }

    private TimeRangeEditor tre = new TimeRangeEditor();
}

