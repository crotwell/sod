/**
 * FixedDataCenterEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.JComponent;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.cache.NSSeismogramDC;
import edu.sc.seis.fissuresUtil.cache.ServerNameDNS;
import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;

public class FixedDataCenterEditor extends ServerEditor implements EditorPlugin{
    public FixedDataCenterEditor(SodGUIEditor owner){ super(owner); }

    protected ServerNameDNS getServerNameDNS(String name, String dns, FissuresNamingService fisName) {
        return new NSSeismogramDC(name, dns, fisName);
    }


    protected ServerNameDNS[] getAllServers(FissuresNamingService fisName) {
        return fisName.getAllSeismogramDC();
    }

    public JComponent getGUI(Element element) throws Exception {
        return getServerBox(element);
    }
}

