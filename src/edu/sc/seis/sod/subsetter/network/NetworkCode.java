package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class NetworkCode implements NetworkSubsetter {

    public NetworkCode(Element config) {
        this.desiredCode = SodUtil.getText(config);
    }

    public StringTree accept(NetworkAttr attr) throws Exception {
        return new StringTreeLeaf(this, attr.get_code().equals(desiredCode));
    }

    private String desiredCode;

    public String getCode() {
        return desiredCode;
    }
}