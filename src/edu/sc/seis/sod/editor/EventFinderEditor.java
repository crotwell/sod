/**
 * EventFinderEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class EventFinderEditor implements EditorPlugin{
    public EventFinderEditor(SodGUIEditor owner){ this.owner = owner; }

    public JComponent getGUI(Element element) throws Exception {
        Box b = Box.createVerticalBox();
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if(n instanceof Element){
                Element el = (Element)n;
                if(el.getTagName().equals("catalog")){
                    i++;
                    while(i < children.getLength() && !(children.item(i) instanceof Element)){
                        i++;
                    }
                    Element nextEl = (Element)children.item(i);
                    if(nextEl.getTagName().equals("contributor")){
                        i++;
                        Box horiz = Box.createHorizontalBox();
                        horiz.add(Box.createHorizontalGlue());
                        horiz.add(owner.getCompForElement(el));
                        horiz.add(Box.createHorizontalStrut(40));
                        horiz.add(owner.getCompForElement(nextEl));
                        horiz.add(Box.createHorizontalGlue());
                        horiz.setBorder(new TitledBorder("Source"));
                        b.add(horiz);
                        continue;
                    }else{ i--; }
                }
                b.add(owner.getCompForElement((Element)n));
            }
        }
        return b;
    }

    private SodGUIEditor owner;

}

