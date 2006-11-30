package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.velocity.PrintlineVelocitizer;


public abstract class AbstractPrintlineProcess {

    public AbstractPrintlineProcess(Element config) throws ConfigurationException {
        filename = DOMHelper.extractText(config, "filename", "");
        template = DOMHelper.extractText(config, "template", getDefaultTemplate());
        velocitizer = new PrintlineVelocitizer(new String[]{filename, template});
    }

    public abstract String getDefaultTemplate();

    protected PrintlineVelocitizer velocitizer;

    protected String filename, template;
    
}
