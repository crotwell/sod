/**
 * EventStatusjava.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.eventArm;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.hibernate.StatefulEvent;

public interface EventMonitor extends SodElement {

    public void setArmStatus(String status) throws Exception;
    
    public void change(StatefulEvent event);
}

