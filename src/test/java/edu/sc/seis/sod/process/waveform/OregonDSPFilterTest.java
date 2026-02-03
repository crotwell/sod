package edu.sc.seis.sod.process.waveform;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.oregondsp.signalProcessing.filter.iir.Butterworth;
import com.oregondsp.signalProcessing.filter.iir.IIRFilter;
import com.oregondsp.signalProcessing.filter.iir.PassbandType;



public class OregonDSPFilterTest {


    @Test
    public void test() {
        int numPoles = 2;
        float delta = 0.01f;
        float lowFreqCorner = 1;
        float highFreqCorner  = 10f;
        PassbandType passband = PassbandType.LOWPASS;
        IIRFilter filter = new Butterworth(numPoles,
                                           passband,
                                           lowFreqCorner,
                                           highFreqCorner,
                                           delta);
        float[] data = new float[14400];
        for (int i = 0; i < data.length; i++) {
            data[i] =  10*(float)(Math.random()-0.5);
        }
        filter.filter(data);
        // note DC offset acts like step, causes ripples in data
        // proper to rmean, filter, add mean back in
        for (int i = 6; i < data.length; i++) {
            assertEquals( 0, data[i], 10, "data i"+i);
        }
    }
    
//    @Test
//    public void testFIR() {
//        EquirippleLowpass fir = new EquirippleLowpass(64, OmegaP, Wp, OmegaS, Ws);
//    }
}
