package edu.sc.seis.sod.process.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.Magnitude;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.display.EventInfoDisplay;
import edu.sc.seis.fissuresUtil.display.ParseRegions;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.eventArm.EventArmProcess;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;

public class PrintLineEventProcess implements EventArmProcess {
    public PrintLineEventProcess (Element config){
        filename = SodUtil.getNestedText(config);
    }

    public void process(EventAccessOperations event) throws IOException {
        String eventStr = regions.getRegionName(event.get_attributes().region);
        try {
            String magString = "";
            Magnitude mag = EventInfoDisplay.getBestForDisplay(event.get_preferred_origin().magnitudes);
            if (mag != null) {
                magString = mag.type+" "+mag.value;
            }
            eventStr =magString+" "+eventStr;
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
    protected static ParseRegions regions = ParseRegions.getInstance();

}// PrintLineEventProcess
