// **********************************************************************
//
//This class represents the PushClient.
//
// **********************************************************************

package edu.sc.seis.sod.subsetter.eventArm;


import edu.sc.seis.sod.*;

import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.utility.*;
import edu.iris.Fissures.model.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosEventChannelAdmin.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CosEventComm.*;

public class PushClient
{
    public PushClient(EventDC eventDC, String channelName)
    {
	try {
       
	   
	    System.out.println("The name of the channel is "+channelName);
	    //create and initialize orb
	    org.omg.CORBA_2_3.ORB orb = (org.omg.CORBA_2_3.ORB) CommonAccess.getCommonAccess().getORB();
	    edu.iris.Fissures.IfEvent.EventChannelFinder eventChannelFinder = null;
	    eventChannelFinder = eventDC.a_channel_finder();
	    // get the reference to the channel..
	    org.omg.CosEventChannelAdmin.ConsumerAdmin consumerAdmin =
		eventChannelFinder.retrieve_channel(channelName);
	    
            // Resolve Root POA
            //
            org.omg.PortableServer.POA rootPOA =
                org.omg.PortableServer.POAHelper.narrow(
                           orb.resolve_initial_references("RootPOA"));
	       
            //
            // Get a reference to the POA manager
            //
            org.omg.PortableServer.POAManager manager = 
                rootPOA.the_POAManager();

	    org.omg.CosEventChannelAdmin.EventChannel channel;
	   
	   

        //
        // Create a persistent POA.
        //
        Policy[] pl = new Policy[2];
        pl[0] = rootPOA.create_lifespan_policy(
	    org.omg.PortableServer.LifespanPolicyValue.PERSISTENT);
        pl[1] = rootPOA.create_id_assignment_policy(
	    org.omg.PortableServer.IdAssignmentPolicyValue.USER_ID);

        POA consumerPOA = rootPOA.create_POA("consumer", manager, pl);

        //
        // Create the servant
        //
        PushConsumer_impl impl = new PushConsumer_impl(orb, consumerPOA, true);

        //
        // Connect the supplier to the POA with the same name each
        // time.
        //
        byte[] oid = ("DefaultConsumer").getBytes();
        consumerPOA.activate_object_with_id(oid, impl);
        org.omg.CosEventComm.PushConsumer consumer = impl._this(orb);

        ProxyPushSupplier supplier = null;
          //
	    // Connect implementation to ProxyPushSupplier.
	supplier = consumerAdmin.obtain_push_supplier();
	supplier.connect_push_consumer(consumer);
	System.out.println("NOw the system will be able to receive the events from the event channel");
	    
        //
        // Activate the POAManager.
        //
        manager.activate();
	

	
	//
	// Run implementation.
	//
	orb.run();
	} catch(Exception e) {
	    System.out.println("Exception occured");

	}
	
    }

}
