/**
 * EventFinderEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.cache.ServerNameDNS;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class EventFinderEditor extends ServerEditor implements EditorPlugin{

    public EventFinderEditor(SodGUIEditor owner){
        super(owner);
    }

    public JComponent getGUI(final Element element) throws Exception {
        Box b = Box.createVerticalBox();
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));

        // deal with servers
        b.add(getServerBox(element));

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if(n instanceof Element){
                Element el = (Element)n;
                if (el.getTagName().equals("name") || el.getTagName().equals("dns")) {
                    // skip as we deal with the server above
                    continue;
                } else if(el.getTagName().equals("catalog")){
                    System.out.println("add catalog to editor");
                    i++;
                    Box horiz = Box.createHorizontalBox();
                    horiz.add(Box.createHorizontalGlue());
                    horiz.add(getOwner().getCompForElement(el));
                    horiz.add(Box.createHorizontalStrut(40));
                    horiz.setBorder(new TitledBorder("Source"));
                    b.add(horiz);
                    while(i < children.getLength() && !(children.item(i) instanceof Element)){
                        i++;
                    }
                    if(i < children.getLength()) {
                        Element nextEl = (Element)children.item(i);
                        if (nextEl.getTagName().equals("contributor")){
                            i++;
                            horiz.add(getOwner().getCompForElement(nextEl));
                            horiz.add(Box.createHorizontalGlue());
                            continue;
                        }
                    }
                }else if(el.getTagName().equals("originDepthRange")){
                    JComponent unitRangeComp = getOwner().getCompForElement((Element)XPathAPI.selectSingleNode(el, "unitRange"));
                    unitRangeComp.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(el.getTagName())));
                    b.add(unitRangeComp);
                    continue;
                } else {
                    b.add(getOwner().getCompForElement((Element)n));
                }
                b.add(Box.createVerticalStrut(10));
            }
        }
        return b;
    }

    protected ServerNameDNS getServerNameDNS(String name, String dns, FissuresNamingService fisName) {
        return new NSEventDC(name, dns, fisName);
    }

    protected ServerNameDNS[] getAllServers(FissuresNamingService fisName) {
        return fisName.getAllEventDC();
    }
}


