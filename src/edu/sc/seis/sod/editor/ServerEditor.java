/**
 * ServerEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.sc.seis.fissuresUtil.cache.NSEventDC;
import edu.sc.seis.fissuresUtil.cache.ServerNameDNS;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;

public abstract class ServerEditor {
    public ServerEditor(SodGUIEditor owner){
        this.owner = owner;
        this.fisName = new FissuresNamingService(owner.getProperties());
        fisName.setNameServiceCorbaLoc(getOwner().getProperties().getProperty("edu.sc.seis.sod.nameServiceCorbaLoc"));
    }

    public SodGUIEditor getOwner() {
        return owner;
    }

    protected abstract ServerNameDNS getServerNameDNS(String name, String dns, FissuresNamingService fisName);

    protected abstract ServerNameDNS[] getAllServers(FissuresNamingService fisName);

    protected Box getServerBox(final Element element) throws TransformerException {
        Box serverBox = Box.createHorizontalBox();
        serverBox.setBorder(new TitledBorder("Server"));
        ServerNameDNSWrapper current = new ServerNameDNSWrapper(getServerNameDNS(XPathAPI.selectSingleNode(element, "dns/text()").getNodeValue(),
                                                                                 XPathAPI.selectSingleNode(element, "name/text()").getNodeValue(),
                                                                                 fisName));
        JComboBox combo = new JComboBox(new Object[] { current});
        serverBox.add(combo);
        serverBox.add(Box.createHorizontalGlue());
        combo.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == e.SELECTED) {
                            ServerNameDNSWrapper selected = (ServerNameDNSWrapper)e.getItem();
                            try {
                                XPathAPI.selectSingleNode(element, "dns/text()").setNodeValue(selected.getServerDNS());
                                XPathAPI.selectSingleNode(element, "name/text()").setNodeValue(selected.getServerName());
                            } catch (TransformerException ex) {
                                // oh well?
                                GlobalExceptionHandler.handle("Can't update server in XML", ex);
                            }
                        }
                    }
                });
        Thread t = new Thread(new ServerLoader(combo, current));
        t.start();
        return serverBox;
    }

    private SodGUIEditor owner;

    protected FissuresNamingService fisName;

    class ServerLoader implements Runnable {
        ServerLoader(JComboBox combo, ServerNameDNS defaultServer) {
            this.defaultServer = defaultServer;
            this.combo = combo;
        }
        public void run() {
            ServerNameDNS[] servers = getAllServers(fisName);
            for (int i = 0; i < servers.length; i++) {
                ServerNameDNSWrapper server = new ServerNameDNSWrapper(servers[i]);
                if ( ! server.equals(defaultServer)) {
                    combo.addItem(server);
                }
            }
        }
        ServerNameDNS defaultServer;
        JComboBox combo;
    }

    class ServerNameDNSWrapper implements ServerNameDNS {
        ServerNameDNSWrapper(ServerNameDNS server) {
            this.server = server;
        }
        public String getServerName() {
            return server.getServerName();
        }

        public String getServerDNS() {
            return server.getServerDNS();
        }
        public String toString() {
            return SimpleGUIEditor.getDisplayName(server.getServerDNS()+"/"+server.getServerName());
        }
        public boolean equals(Object o) {
            if(this == o){ return true; }
            if(o instanceof ServerNameDNSWrapper){
                ServerNameDNSWrapper otherWrapper = (ServerNameDNSWrapper)o;
                return otherWrapper.getServerDNS().equals(getServerDNS()) &&
                    otherWrapper.getServerName().equals(getServerName());
            }
            return false;
        }

        public int hashCode() {
            int result = 37;
            result = result * 42 + getServerDNS().hashCode();
            return  result * 42 + getServerName().hashCode();
        }
        private ServerNameDNS server;
    }
}

