package edu.sc.seis.sod;

import org.apache.log4j.BasicConfigurator;
import edu.sc.seis.sod.status.eventArm.MapEventStatus;

/**
 * @author groves Created on Aug 20, 2004
 */
public class EventCircleGenerator {

	public static void main(String[] args) {
        BasicConfigurator.configure();
		new MapEventStatus("events.png", false);
	}
}