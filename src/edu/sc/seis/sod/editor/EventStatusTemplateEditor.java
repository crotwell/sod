/**
 * EventStatusTemplateEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import javax.swing.JComponent;
import org.w3c.dom.Element;



public class EventStatusTemplateEditor implements EditorPlugin{

    public EventStatusTemplateEditor(){
//      for (int i = 0; i < visibleValues.length; i++) {
//          valueMap.put(visibleValues[i], xlinkValues[i]);
//          reverseValueMap.put(xlinkValues[i], visibleValues[i]);
//      }
    }

    public JComponent getGUI(Element element) throws Exception {
        return EditorUtil.getBoxWithLabel(element);


        //        Box panel = Box.createHorizontalBox();
        //        panel.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        //        final JComboBox box = new JComboBox(visibleValues);
        //        Node n = XPathAPI.selectSingleNode(element, "eventConfig/@xlink:href");
        //        final Attr xlink = (Attr)n;
        //        box.addActionListener(new ActionListener(){
        //                    public void actionPerformed(ActionEvent e) {
        //                        Object item = box.getSelectedItem();
        //                        xlink.setValue((String)valueMap.get(item));
        //                    }
        //
        //                });
        //        box.setSelectedItem(reverseValueMap.get(xlink.getValue()));
        //        panel.add(new JLabel("Sort by:"));
        //        panel.add(box);
        //        panel.add(Box.createHorizontalGlue());
        //
        //        return panel;
    }

//    private Map valueMap = new HashMap();
//    private Map reverseValueMap = new HashMap();
//    public static String prefix = "jar:edu/sc/seis/sod/data/templates/eventArm/";
//    protected String[] visibleValues = {"Depth", "Time", "Magnitude"};
//    protected String[] xlinkValues = {prefix + "depthSorted.xml", prefix + "timeSorted.xml", prefix + "magSorted.xml"};
}

