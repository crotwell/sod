package edu.sc.seis.sod.process.eventArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;

public class NullEventProcess implements EventProcess {
    public NullEventProcess (){}

    public void process(EventAccessOperations event) {}
}// NullEventProcess
