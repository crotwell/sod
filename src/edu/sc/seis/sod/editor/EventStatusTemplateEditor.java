/**
 * EventStatusTemplateEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;



public class EventStatusTemplateEditor implements EditorPlugin{

    public EventStatusTemplateEditor(){
        for (int i = 0; i < visibleValues.length; i++) {
            valueMap.put(visibleValues[i], xlinkValues[i]);
            reverseValueMap.put(xlinkValues[i], visibleValues[i]);
        }
    }

    public JComponent getGUI(Element element) throws Exception {
        Box panel = Box.createHorizontalBox();
        panel.setBorder(new TitledBorder("EventStatusTemplate"));
        final JComboBox box = new JComboBox(new String[]{"Depth", "Time", "Magnitude"});
        Node n = XPathAPI.selectSingleNode(element, "eventConfig/@xlink:href");
        final Attr xlink = (Attr)n;
        box.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        Object item = box.getSelectedItem();
                        xlink.setValue((String)valueMap.get(item));
                    }

                });
        box.setSelectedItem(reverseValueMap.get(xlink.getValue()));
        panel.add(new JLabel("Sort by:"));
        panel.add(box);
        panel.add(Box.createHorizontalGlue());

        return panel;
    }

    private Map valueMap = new HashMap();
    private Map reverseValueMap = new HashMap();
    public static String prefix = "jar:edu/sc/seis/sod/data/templates/eventArm/";
    protected String[] visibleValues = {"Depth", "Time", "Magnitude"};
    protected String[] xlinkValues = {prefix + "depthSorted.xml", prefix + "timeSorted.xml", prefix + "magSorted.xml"};
}

