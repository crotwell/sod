package edu.sc.seis.sod.process.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.EventArmProcess;
import edu.sc.seis.sod.SodUtil;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;

public class PrintLineEventProcess implements EventArmProcess {
    public PrintLineEventProcess (Element config){
        filename = SodUtil.getNestedText(config);
    }

    public void process(EventAccessOperations event, CookieJar cookies) throws IOException {
        String eventStr =
            regions.getRegionName(event.get_attributes().region);
        try {
            eventStr =event.get_preferred_origin().magnitudes[0].type+" "+
                event.get_preferred_origin().magnitudes[0].value+" "+eventStr;
            eventStr+=" "+event.get_preferred_origin().origin_time.date_time;
        } catch (NoPreferredOrigin e) {
            eventStr+=" No Pref Origin!";
        } // end of try-catch


        if (filename != null) {
            FileWriter fwriter = new FileWriter("_my_event_temp_", true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(eventStr, 0, eventStr.length());
            bwriter.newLine();
            bwriter.close();
        } else {
            System.out.println(eventStr);
        } // end of else

    }

    protected String filename = null;

    protected static edu.sc.seis.fissuresUtil.display.ParseRegions regions
        = edu.sc.seis.fissuresUtil.display.ParseRegions.getInstance();

}// PrintLineEventProcess
