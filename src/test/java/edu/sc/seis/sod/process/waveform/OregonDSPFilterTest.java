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
        float delta = 1.0f;
        float lowFreqCorner = 0;
        float highFreqCorner  = 1/60f;
        PassbandType passband = PassbandType.LOWPASS;
        IIRFilter filter = new Butterworth(numPoles,
                                           passband,
                                           lowFreqCorner,
                                           highFreqCorner,
                                           delta);
        float[] data = new float[14400];
        for (int i = 0; i < data.length; i++) {
            data[i] = 2000 + 10*(float)(Math.random()-0.5);
        }
        filter.filter(data);
        for (int i = 0; i < data.length; i++) {
            assertEquals( 2000, data[i], 10, "data i"+i);
        }
    }
    
//    @Test
//    public void testFIR() {
//        EquirippleLowpass fir = new EquirippleLowpass(64, OmegaP, Wp, OmegaS, Ws);
//    }
}
