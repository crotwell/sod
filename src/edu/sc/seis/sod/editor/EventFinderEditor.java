/**
 * EventFinderEditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class EventFinderEditor implements EditorPlugin{
    public EventFinderEditor(SodGUIEditor owner){ this.owner = owner; }

    public JComponent getGUI(final Element element) throws Exception {
        Box b = Box.createVerticalBox();
        b.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getTagName())));

        // deal with servers
        Box serverBox = Box.createHorizontalBox();
        serverBox.setBorder(new TitledBorder("Server"));
        b.add(serverBox);
        ServerNameDNS current = new ServerNameDNS(XPathAPI.selectSingleNode(element, "name/text()").getNodeValue(),
                                                  XPathAPI.selectSingleNode(element, "dns/text()").getNodeValue());
        JComboBox combo = new JComboBox(new Object[] { current});
        serverBox.add(combo);
        combo.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == e.SELECTED) {
                            ServerNameDNS selected = (ServerNameDNS)e.getItem();
                            try {
                                XPathAPI.selectSingleNode(element, "name/text()").setNodeValue(selected.name);
                                XPathAPI.selectSingleNode(element, "dns/text()").setNodeValue(selected.dns);
                            } catch (TransformerException ex) {
                                // oh well?
                                GlobalExceptionHandler.handle("Can't update server in XML", ex);
                            }
                        }
                    }
                });
        Thread t = new Thread(new ServerLoader(combo, current));
        t.start();

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if(n instanceof Element){
                Element el = (Element)n;
                if (el.getTagName().equals("name") || el.getTagName().equals("dns")) {
                    // skip as we deal with the server above
                    continue;
                } else if(el.getTagName().equals("catalog")){
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
                }else if(el.getTagName().equals("originDepthRange")){
                    JComponent unitRangeComp = owner.getCompForElement((Element)XPathAPI.selectSingleNode(el, "unitRange"));
                    unitRangeComp.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(el.getTagName())));
                    b.add(unitRangeComp);
                    continue;
                }
                b.add(owner.getCompForElement((Element)n));
                b.add(Box.createVerticalStrut(10));
            }
        }
        return b;
    }

    private SodGUIEditor owner;

    class ServerNameDNS {
        ServerNameDNS(String name, String dns) {
            this.name = name;
            this.dns = dns;
        }
        public String toString() {
            return SimpleGUIEditor.getDisplayName(dns+"/"+name);
        }
        public boolean equals(Object o) {
            if ( ! (o instanceof ServerNameDNS)) {
                return false;
            }
            ServerNameDNS other = (ServerNameDNS)o;
            return (other.name.equals(name) && other.dns.equals(dns));
        }
        public int hashCode() {
            return (name+dns).hashCode();
        }
        String name;
        String dns;
    }

    class ServerLoader implements Runnable {
        ServerLoader(JComboBox combo, ServerNameDNS defaultServer) {
            this.defaultServer = defaultServer;
            this.combo = combo;
        }
        public void run() {
            System.out.println("fisName: "+owner.getProperties().getProperty("edu.sc.seis.sod.nameServiceCorbaLoc"));
            FissuresNamingService fname = new FissuresNamingService(owner.getProperties());
            fname.setNameServiceCorbaLoc(owner.getProperties().getProperty("edu.sc.seis.sod.nameServiceCorbaLoc"));
            NSEventDC[] eventServers = fname.getAllEventDC();
            for (int i = 0; i < eventServers.length; i++) {
                ServerNameDNS server = new ServerNameDNS(eventServers[i].getServerName(),
                                                         eventServers[i].getServerDNS());
                if ( ! server.equals(defaultServer)) {
                    combo.addItem(server);
                }
            }
        }
        ServerNameDNS defaultServer;
        JComboBox combo;
    }
}


