package edu.sc.seis.sod.model.seismogram;


final public class EncodedData 
{
    public
    EncodedData()
    {
    }

    public
    EncodedData(short compression,
                byte[] values,
                int num_points,
                boolean byte_order)
    {
        this.compression = compression;
        this.values = values;
        this.num_points = num_points;
        this.byte_order = byte_order;
    }

    public short compression;
    public byte[] values;
    public int num_points;
    public boolean byte_order;
}
