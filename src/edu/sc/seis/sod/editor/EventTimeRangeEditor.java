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



public class EventTimeRangeEditor implements EditorPlugin{

    public JComponent getGUI(Element element) throws Exception {
        JComponent gui = timeRangeEditor.getGUI((Element)XPathAPI.selectSingleNode(element, "timeRange"));
        gui.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        return gui;
    }


    private static TimeRangeEditor timeRangeEditor = new TimeRangeEditor();
}

