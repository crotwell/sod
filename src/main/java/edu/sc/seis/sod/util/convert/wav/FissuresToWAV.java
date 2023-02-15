package edu.sc.seis.sod.util.convert.wav;

import java.io.DataOutput;
import java.io.IOException;

import javax.sound.sampled.Clip;
import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.seisFile.mseed.Utility;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.SamplingImpl;
import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/**
 * FissuresToWAV.java
 *  http://ccrma-www.stanford.edu/CCRMA/Courses/422/projects/WaveFormat/
 *
 *
 * Created: Wed Feb 19 15:35:06 2003
 *
 * @author Philip Crotwell
 * @version 1.0
 */
public class FissuresToWAV {

    private int chunkSize, numChannels, sampleRate, speedUp, bitsPerSample,
        blockAlign, byteRate, subchunk2Size;
    private Clip clip;
    LocalSeismogramImpl seis;
    private EventListenerList listenerList = new EventListenerList();

    public FissuresToWAV(LocalSeismogramImpl seis, int speedUp) {
        this.seis = seis;
        this.speedUp = speedUp;
        numChannels = 1;
        bitsPerSample = 16;
        blockAlign = numChannels * (bitsPerSample/8);
    }

    public void writeWAV(DataOutput out, TimeRange tr) throws IOException, FissuresException, CodecException  {
        updateInfo();
        writeChunkData(out);
        writeWAVData(out);
    }

    private void updateInfo(){
        chunkSize = 36 + 2*seis.getNumPoints();
        subchunk2Size = seis.getNumPoints() * blockAlign;
        sampleRate = calculateSampleRate(seis.getSampling());
        byteRate = sampleRate * blockAlign;
    }

    public void setSpeedUp(int newSpeed){
        speedUp = newSpeed;
        updateInfo();
    }

    private void writeChunkData(DataOutput out) throws IOException{
        out.writeBytes("RIFF"); //ChunkID

        //ChunkSize
        writeLittleEndian(out, chunkSize);

        out.writeBytes("WAVE"); //Format

        // write fmt subchunk
        out.writeBytes("fmt "); //Subchunk1ID
        writeLittleEndian(out, 16); //Subchunk1Size
        writeLittleEndian(out, (short)1); // Audioformat = linear quantization, PCM
        writeLittleEndian(out, (short)numChannels); // NumChannels
        writeLittleEndian(out, sampleRate); // SampleRate
        writeLittleEndian(out, byteRate); // byte rate
        writeLittleEndian(out, (short)blockAlign); // block align
        writeLittleEndian(out, (short)bitsPerSample); // bits per sample

        // write data subchunk
        out.writeBytes("data");
        writeLittleEndian(out, subchunk2Size); // subchunk2 size
    }

    private void writeWAVData(DataOutput out) throws IOException, CodecException, FissuresException {

        //calculate maximum amplification factor to avoid either
        //clipping or dead quiet
        double max = seis.getMaxValue().getValue();
        double min = seis.getMinValue().getValue();
        double absMax = Double.MAX_VALUE;
        if (Math.abs(min) > Math.abs(max)){
            absMax = Math.abs(min);
        }
        else{
            absMax = Math.abs(max);
        }
        double amplification = (32000.0/absMax);

            try{
                if (seis.can_convert_to_long()) {
                    int[] data = seis.get_as_longs();
                    for (int i = 0; i < data.length; i++) {
                        writeLittleEndian(out, (short)(amplification * data[i]));
                    }
                } else {
                    double[] data = seis.get_as_doubles();
                    for (int i = 0; i < data.length; i++) {
                        writeLittleEndian(out, (short)(amplification * data[i]));
                    }
                }
            }
            catch(NullPointerException e){
                writeLittleEndian(out, (short)0);
            }
            catch(ArrayIndexOutOfBoundsException e){
                writeLittleEndian(out, (short)0);
            }
        
    }

    public int calculateSampleRate(SamplingImpl sampling){
        QuantityImpl freq = sampling.getFrequency();
        freq = freq.convertTo(UnitImpl.HERTZ);
        int sampleRate = (int)(freq.getValue() * speedUp);
        while (sampleRate > 48000){
            setSpeedUp(speedUp/2);
            logger.debug("speedUp = " + speedUp);
            sampleRate = (int)(freq.getValue() * speedUp);
            logger.debug("sampleRate = " + sampleRate);
        }
        return sampleRate;
    }

    protected static void writeLittleEndian(DataOutput out, int value)
        throws IOException {
        byte[] tmpBytes;
        tmpBytes = Utility.intToByteArray(value);
        out.write(tmpBytes[3]);
        out.write(tmpBytes[2]);
        out.write(tmpBytes[1]);
        out.write(tmpBytes[0]);
    }

    protected static void writeLittleEndian(DataOutput out, short value)
        throws IOException {
        byte[] tmpBytes;
        tmpBytes = Utility.intToByteArray((int)value);
        out.write(tmpBytes[3]);
        out.write(tmpBytes[2]);
    }

    private static Logger logger = LoggerFactory.getLogger(FissuresToWAV.class.getName());

} // FissuresToWAV

