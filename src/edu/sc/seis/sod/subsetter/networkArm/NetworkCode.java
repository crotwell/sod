package edu.sc.seis.sod.subsetter.networkArm;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.SodUtil;

public class NetworkCode implements NetworkSubsetter {

    public NetworkCode(Element config) {
        this.desiredCode = SodUtil.getText(config);
    }

    public boolean accept(NetworkAttr attr) throws Exception {
        return attr.get_code().equals(desiredCode);
    }

    private String desiredCode;
}