package edu.sc.seis.sod.hibernate;

import org.omg.CORBA_2_3.ORB;

import edu.iris.Fissures.IfNetwork.InstrumentationHelper;
import edu.iris.Fissures.model.AllVTFactory;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import javassist.CtField.Initializer;


public class InstrumentationBlob {
    
    /** for hibernate. */
    protected InstrumentationBlob() {}
    
    public InstrumentationBlob(ChannelImpl chan, Instrumentation inst) {
        this.chan = chan;
        this.inst = inst;
    }
    
    public byte[] getInstrumentationAsBlob() {
        org.jacorb.orb.CDROutputStream cdrOut = new org.jacorb.orb.CDROutputStream(Initializer.getORB());
        InstrumentationHelper.write(cdrOut, inst);
        return cdrOut.getBufferCopy();
    }
    
    protected void setInstrumentationAsBlob(byte[] instBytes) {
        if (instBytes.length > 0) {
            org.jacorb.orb.CDRInputStream cdrIn = new org.jacorb.orb.CDRInputStream(getORB(), instBytes);
            inst = InstrumentationHelper.read(cdrIn);
        } else {
            inst = null;
        }
    }
        
    public Instrumentation getInstrumentation() {
        return inst;
    }
    
    public ChannelImpl getChannel() {
        return chan;
    }
    
    public void setChannel(ChannelImpl chan) {
        this.chan = chan;
    }

    ChannelImpl chan;
    Instrumentation inst;
    int dbid;
    
    public int getDbid() {
        return dbid;
    }

    
    public void setDbid(int dbid) {
        this.dbid = dbid;
    }
    
    public static ORB getORB() {
        if (orb == null) {
            // better if it is set, but...
            orb = Initializer.getORB();
            new AllVTFactory().register(orb);
        }
        return orb;
    }
    
    public static void setORB(ORB o) {
        orb = o;
    }
    
    static ORB orb = null;
}
