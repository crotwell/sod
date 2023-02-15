package edu.sc.seis.sod.model.seismogram;

import edu.sc.seis.sod.model.common.FissuresException;

/**
 * DataAppend.java
 *
 *
 * Created: Sun Jan 23 22:35:50 2000
 *
 * @author Philip Crotwell
 * @version
 */

public class DataAppend  {

    public DataAppend() {

    }

    public static void append(TimeSeriesDataSel firstData,
                  int[] second)
    throws FissuresException
    {
    if (firstData.discriminator() == TimeSeriesType.TYPE_LONG) {
        int[] first = firstData.int_values();
            int[] newData = new int[first.length+second.length];
            System.arraycopy(first, 0,
                             newData, 0,
                             first.length);
            System.arraycopy(second, 0,
                             newData, first.length,
                             second.length);
            firstData.int_values(newData);
    } else {
        throw new FissuresException("Can't append int to data type "
                                                      +firstData.discriminator());
    }
    }

    public static void append(TimeSeriesDataSel firstData,
                  short[] second)
    throws FissuresException
    {
    if (firstData.discriminator() == TimeSeriesType.TYPE_SHORT) {
        short[] first = firstData.sht_values();
            short[] newData = new short[first.length+second.length];
            System.arraycopy(first, 0,
                             newData, 0,
                             first.length);
            System.arraycopy(second, 0,
                             newData, first.length,
                             second.length);
            firstData.sht_values(newData);
    } else {
        throw new FissuresException("Can't append short to data type "
                                                      +firstData.discriminator());
    }
    }

    public static void append(TimeSeriesDataSel firstData,
                  float[] second)
    throws FissuresException
    {
    if (firstData.discriminator() == TimeSeriesType.TYPE_FLOAT) {
        float[] first = firstData.flt_values();
            float[] newData = new float[first.length+second.length];
            System.arraycopy(first, 0,
                             newData, 0,
                             first.length);
            System.arraycopy(second, 0,
                             newData, first.length,
                             second.length);
            firstData.flt_values(newData);
    } else {
        throw new FissuresException("Can't append float to data type "
                                                      +firstData.discriminator());
    }
    }

    public static void append(TimeSeriesDataSel firstData,
                  double[] second)
    throws FissuresException
    {
    if (firstData.discriminator() == TimeSeriesType.TYPE_DOUBLE) {
        double[] first = firstData.dbl_values();
            double[] newData = new double[first.length+second.length];
            System.arraycopy(first, 0,
                             newData, 0,
                             first.length);
            System.arraycopy(second, 0,
                             newData, first.length,
                             second.length);
            firstData.dbl_values(newData);
    } else {
        throw new FissuresException("Can't append double to data type "
                                                      +firstData.discriminator());
    }
    }

    public static void append(TimeSeriesDataSel firstData,
                  EncodedData[] second)
    throws FissuresException
    {
    if (firstData.discriminator() == TimeSeriesType.TYPE_ENCODED) {
            EncodedData[] first = firstData.encoded_values();
            EncodedData[] newData =
              new EncodedData[first.length+second.length];
            System.arraycopy(first, 0,
                             newData, 0,
                             first.length);
            System.arraycopy(second, 0,
                             newData, first.length,
                             second.length);
            firstData.encoded_values(newData);
    } else {
        throw new FissuresException("Can't append encoded to data type "
                                                      +firstData.discriminator().value());
    }
    }

    public static void append(TimeSeriesDataSel firstData,
                  EncodedData second)
    throws FissuresException
    {
    if (firstData.discriminator() == TimeSeriesType.TYPE_ENCODED) {
            EncodedData[] first = firstData.encoded_values();
            EncodedData[] newData =
              new EncodedData[first.length+1];
            System.arraycopy(first, 0,
                             newData, 0,
                             first.length);
            newData[first.length] = second;
            firstData.encoded_values(newData);
    } else {
        throw new FissuresException("Can't append encoded to data type "
                                                      +firstData.discriminator().value());
    }
    }

} // DataAppend
