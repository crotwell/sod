/**
 * NativeFFTTest.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveform.vector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.sc.seis.fissuresUtil.freq.Cmplx;

public class OregonDspFFTTest  {

    @Test
    public void testForward() {
        float[] data = new float[1024];
        data[3] = 6;
        data[23] = 9;
        data[67] = 10;
        data[100] = 2;
        data[200] = -1.5f;
        data[300] = .25f;

        float[] nativeData = new float[data.length];
        float[] javaData = new float[data.length];

        System.arraycopy(data, 0, nativeData, 0, data.length);
        System.arraycopy(data, 0, javaData, 0, data.length);

        nativeData = OregonDspFFT.forward(nativeData);

        Cmplx[] cData = Cmplx.fft(javaData);


//        for (int i = 0; i < cData.length/2; i++) {
//            System.out.println("real i="+i+" "+ (float)cData[i].real()+"      "+  nativeData[2*i]);
//            System.out.println("imag i="+i+" "+ (float)cData[i].imag()+"      "+  nativeData[2*i+1]);
//        }
//        for (int i = cData.length/2; i < cData.length; i++) {
//            System.out.println("real i="+i+" "+ (float)cData[i].real());
//            System.out.println("imag i="+i+" "+ (float)cData[i].imag());
//        }
        

        System.out.println("real i=0"+"  "+ (float)cData[0].real()+"  "+ nativeData[0]);
        System.out.println("imag i=nyq"+"  "+ (float)cData[cData.length/2].real()+"  "+ nativeData[nativeData.length/2]);
        for (int i = 1; i < cData.length/2; i++) {
            System.out.println("real i="+i+"  "+ (float)cData[i].real()+"  "+ nativeData[i]);
            System.out.println("imag i="+i+"  "+ (float)cData[i].imag()+"  "+ nativeData[nativeData.length-i]);
        }

        // check 0, and F_n/2 is in imaginary of F_0
        assertEquals("real F0", nativeData[0], cData[0].real(), 0.00001f);
        assertEquals("real F_n/2", nativeData[cData.length/2], cData[cData.length/2].real(), 0.00001f);
        for (int i = 1; i < data.length/2; i++) {
            System.out.println(i+"  "+nativeData[i]+"  "+cData[i].real());
            assertEquals("real i="+i, (float)cData[i].real(), nativeData[i], 0.001f);
            assertEquals("imag i="+i, (float)cData[i].imag(), nativeData[nativeData.length-i], 0.001f);
        }
    }

    @Test
    public void testVsCmplxCorrelate() {
        float[] fdata = new float[128];
        float[] gdata = new float[128];
        fdata[5]=1;
        fdata[6]=2;
        fdata[10]=10;
        gdata[43]=7;
        gdata[44]=13;
        gdata[50]=20;
        float[] nativecorr = OregonDspFFT.correlate(fdata, gdata);
        float[] cmplxcorr = Cmplx.correlate(fdata, gdata);
        assertArrayEquals(nativecorr, cmplxcorr, 0.001f);
    }

    @Test
    public void testVsCmplxConvolve() {
        float[] fdata = new float[128];
        float[] gdata = new float[128];
        fdata[5]=1;
        fdata[6]=2;
        fdata[10]=10;
        gdata[43]=7;
        gdata[44]=13;
        gdata[50]=20;
        float delta = 0.5f;
        float[] nativecorr = OregonDspFFT.convolve(fdata, gdata, delta);
        float[] cmplxcorr = Cmplx.convolve(fdata, gdata, delta);
        assertArrayEquals(nativecorr, cmplxcorr, 0.001f);
    }

    @Test
    public void testRoundTrip() {

        float[] data = new float[128];
        data[40] = 1;

        float[] nativeData = new float[data.length];

        nativeData = OregonDspFFT.forward(data);
        nativeData = OregonDspFFT.inverse(nativeData);
        assertArrayEquals("round trip", data, nativeData, 0.001f);
    }

    @Test
    public void testCorrelation() {
        float[] fData = { 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        float[] gData = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        float[] corr = OregonDspFFT.correlate(fData, gData);
        assertEquals("lag 0", 0f, corr[0], 0.00001f);
        assertEquals("lag 1", 2f, corr[1], 0.00001f);
        assertEquals("lag 2", 0f, corr[2], 0.00001f);
        assertEquals("lag 3", 0f, corr[3], 0.00001f);
    }

    @Test
    public void testConvolve() {
        float[] fData = { 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        float[] gData = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        float[] corr = OregonDspFFT.convolve(fData, gData, 1);
        assertEquals("lag 0", 0f, corr[0], 0.00001f);
        assertEquals("lag 1", 0f, corr[1], 0.00001f);
        assertEquals("lag 2", 0f, corr[2], 0.00001f);
        assertEquals("lag 3", 2f, corr[3], 0.00001f);
        assertEquals("lag 4", 0f, corr[4], 0.00001f);
        assertEquals("lag 5", 0f, corr[5], 0.00001f);
        assertEquals("lag 6", 0f, corr[6], 0.00001f);
        assertEquals("lag 7", 0f, corr[7], 0.00001f);
    }

}

