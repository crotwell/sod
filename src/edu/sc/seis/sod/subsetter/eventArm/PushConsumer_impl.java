// **********************************************************************
//
// Copyright (c) 2000
// Object Oriented Concepts, Inc.
// Billerica, MA, USA
//
// All Rights Reserved
//
// **********************************************************************

package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfEvent.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.CosEventComm.*;
 
public class PushConsumer_impl extends PushConsumerPOA
{
    private  org.omg.CORBA_2_3.ORB orb_; // The ORB
    private  org.omg.PortableServer.POA poa_; // My POA
    private boolean slow_; // Is this a slow consumer?

    public PushConsumer_impl(org.omg.CORBA_2_3.ORB orb, org.omg.PortableServer.POA poa, boolean slow, EventArm eventArm)
    {
	orb_ = orb;
	poa_ = poa;
	slow_ = slow;
	this.eventArm = eventArm;
    }

    // ------------------------------------------------------------------
    // Standard IDL to Java Mapping
    // ------------------------------------------------------------------

    public void
    push(Any any)
    {
	try
	{
	    //Extract EventNotify from ANY
	    EventNotify eventNotify = EventNotifyHelper.extract(any);
	    System.out.println("THe name of the event is ");
	    eventArm.handleEventAttrSubsetter(eventNotify.the_event, eventNotify.the_event.get_attributes());
	    //Start.getEventQueue().push(eventNotify.the_event);

	}
	catch(Exception ex)
	{

		ex.printStackTrace();
	}
	if(slow_)
	{
	    try
	    {
		Thread.sleep(2000);
	    }
	    catch(InterruptedException e)
	    {
	    }
	}
    }

    public void
    disconnect_push_consumer()
    {
        byte[] oid = null;
        try
        {
            oid = poa_.servant_to_id(this);
        }
        catch(ServantNotActive ex)
        {
            throw new RuntimeException();
        }
        catch(WrongPolicy ex)
        {
            throw new RuntimeException();
        }         

        try
        {
            poa_.deactivate_object(oid);
        }
        catch(ObjectNotActive ex)
        {
            throw new RuntimeException();
        }
        catch(WrongPolicy ex)
        {
            throw new RuntimeException();
        }

        orb_.shutdown(false);
    }

    public POA
    _default_POA()
    {
	return poa_;
    }
    private EventArm eventArm;
}
