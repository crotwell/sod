package edu.sc.seis.sod.process.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.eventArm.EventArmProcess;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.TemplateFileLoader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;

public class PrintLineEventProcess implements EventArmProcess {
    public PrintLineEventProcess (Element config) throws TransformerException, ConfigurationException{
        Element fileEl = (Element)XPathAPI.selectSingleNode(config, "filename");
        if(fileEl != null){
            filename = SodUtil.getNestedText(fileEl);
        }
        //Use the config from the current config file if possible, otherwise
        //load the default
        Element formatEl = (Element)XPathAPI.selectSingleNode(config, "eventFormat");
        if(formatEl == null){
            try {
                formatEl = TemplateFileLoader.getTemplate(getClass().getClassLoader(),
                                                          "jar:edu/sc/seis/sod/data/defaultEventPrintFormat.xml");

            } catch (Exception e) {
                GlobalExceptionHandler.handle("Trouble loading the default event print format, using EventFormatter's default",
                                              e);
                ef = EventFormatter.getDefaultFormatter();
            }
        }
        ef = new EventFormatter(formatEl, false);
    }

    public void process(EventAccessOperations event) throws IOException {
        String eventStr = ef.getResult(event);
        if (filename != null) {
            FileWriter fwriter = new FileWriter(filename, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(eventStr);
            bwriter.newLine();
            bwriter.close();
        } else {
            System.out.println(eventStr);
        } // end of else
    }

    private EventFormatter ef;
    private String filename = null;
}// PrintLineEventProcess
