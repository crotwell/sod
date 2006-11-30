package edu.sc.seis.sod.subsetter.network;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAttr;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;


public class PrintlineNetworkProcess extends AbstractPrintlineProcess implements
        NetworkSubsetter {

    public PrintlineNetworkProcess(Element config) throws ConfigurationException {
        super(config);
    }

    public static final String DEFAULT_TEMPLATE = "Network: $network";

    public String getDefaultTemplate() {
        return DEFAULT_TEMPLATE;
    }

    public StringTree accept(NetworkAttr attr) throws Exception {
        velocitizer.evaluate(filename, template, attr);
        return new StringTreeLeaf(this, true);
    }
}
