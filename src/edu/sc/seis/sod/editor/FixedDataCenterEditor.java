/**
 * FixedDataCenterEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;
import edu.iris.Fissures.IfSeismogramDC.DataCenter;

public class FixedDataCenterEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        return null;
    }

    private HashMap map;

    protected void loadFromNameService() throws ConfigurationException {
        FissuresNamingService fisname = CommonAccess.getCommonAccess().getFissuresNamingService();
        //DataCenter[] allDC = fisname.getAllObjects("SeismogramDC", null);
    }

}

