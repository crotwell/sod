package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.cache.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

import org.w3c.dom.*;

public class WaveFormArm implements Runnable {

	public WaveFormArm(Element config) {
		System.out.println("WAVE FORM ARM STARTED ");
		Thread thread = new Thread(this);
		thread.start();	
	}
	
	public void run() {
		while(true){
	//	System.out.println("RETRIEVED THE EVENT ACCESS FROM EVENT QUEUE");
		try {
		Thread.sleep(3000);	
		} catch(Exception e){}
		EventAccess eventAccess = EventAccessHelper.narrow(Start.getEventQueue().pop());	
		if(eventAccess == null);// System.out.println("EventACCESS is NULL");
		else System.out.println("Event Access is VALID");
		}
	}
}
