/**
 * NetworkFinderEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.sc.seis.fissuresUtil.cache.NSNetworkDC;
import edu.sc.seis.fissuresUtil.cache.ServerNameDNS;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class NetworkFinderEditor extends ServerEditor implements EditorPlugin{

    public NetworkFinderEditor(SodGUIEditor owner){
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
                }
                b.add(getOwner().getCompForElement((Element)n));
                b.add(Box.createVerticalStrut(10));
            }
        }
        return b;
    }

    protected ServerNameDNS getServerNameDNS(String name, String dns, FissuresNamingService fisName) {
        return new NSNetworkDC(name, dns, fisName);
    }

    protected ServerNameDNS[] getAllServers(FissuresNamingService fisName) {
        return fisName.getAllNetworkDC();
    }
}


