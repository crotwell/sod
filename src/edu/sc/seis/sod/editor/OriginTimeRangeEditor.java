/**
 * EventTimeRangeEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;



public class OriginTimeRangeEditor implements EditorPlugin{

    public JComponent getGUI(Element element) throws Exception {
        JComponent gui = timeRangeEditor.getGUI(element);
        gui.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        return gui;
    }


    private static TimeRangeEditor timeRangeEditor = new TimeRangeEditor();
}

