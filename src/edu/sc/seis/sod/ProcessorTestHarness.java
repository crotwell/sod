package edu.sc.seis.sod;

import java.io.IOException;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.hibernate.EventDB;
import edu.sc.seis.sod.process.waveform.ForkProcess;
import edu.sc.seis.sod.process.waveform.RecordSectionDisplayGenerator;
import edu.sc.seis.sod.process.waveform.vector.ORWaveformProcessWrapper;
import edu.sc.seis.sod.process.waveform.vector.VectorForkProcess;
import edu.sc.seis.sod.process.waveform.vector.WaveformVectorAlwaysSuccess;

public class ProcessorTestHarness {

    public static void main(String[] args) throws NoPreferredOrigin, IOException, NotFound, Exception {
        Start.RUN_ARMS = false;
        Start.main(args);
        AbstractWaveformRecipe arm = Start.getWaveformRecipe();
        WaveformVectorAlwaysSuccess alwaysSuccess = (WaveformVectorAlwaysSuccess)((MotionVectorArm)arm).getProcesses()[20];
        VectorForkProcess fork = (VectorForkProcess)alwaysSuccess.getWrappedProcessors()[0];
        WaveformVectorAlwaysSuccess internalSuccess = (WaveformVectorAlwaysSuccess)fork.getWrappedProcessors()[1];
        ORWaveformProcessWrapper orWrapper = (ORWaveformProcessWrapper)internalSuccess.getWrappedProcessors()[0];
        ForkProcess internalFork = (ForkProcess)orWrapper.getWrappedProcess();
        RecordSectionDisplayGenerator gen = (RecordSectionDisplayGenerator)internalFork.getWrappedProcessors()[1];
        System.out.println(gen);
        EventDB events = new EventDB();
        gen.makeRecordSection(events.getEvent(67435));
    }
}
