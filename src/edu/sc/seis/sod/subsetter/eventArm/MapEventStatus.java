package edu.sc.seis.sod.subsetter.eventArm;
import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.sc.seis.fissuresUtil.display.EQDataEvent;
import edu.sc.seis.fissuresUtil.map.OpenMap;
import edu.sc.seis.fissuresUtil.map.layers.EventLayer;
import org.w3c.dom.Element;

/**
 * MapEventStatus.java
 *
 * @author Created by Philip Oliver-Paull
 */
public class MapEventStatus implements SodElement, EventStatus
{
	OpenMap map = new OpenMap("edu/sc/seis/fissuresUtil/data/maps/dcwpo-browse");
	EventLayer events;
	String fileLoc;
		
	public MapEventStatus(Element element){
		fileLoc = element.getAttribute("xlink:link");
		events = new EventLayer(map.getMapBean());
		map.setEventLayer(events);
	}
	
	/**
	 * Method change
	 *
	 * @param    event               an EventAccessOperations
	 * @param    status              a  RunStatus
	 *
	 */
	public void change(EventAccessOperations event, RunStatus status)
	{
		if (status == RunStatus.PASSED){
			events.eventDataChanged(new EQDataEvent(this, new EventAccessOperations[]{event}));
			map.writeMapToPNG(fileLoc);
		}
		
	}
	
	/**
	 * Method setArmStatus
	 *
	 * @param    status              a  String
	 *
	 */
	public void setArmStatus(String status)
	{
		// noImpl
	}
	
}

