package edu.sc.seis.sod.hibernate;

import java.util.List;

import edu.sc.seis.sod.model.common.MicroSecondTimeRange;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.station.ChannelId;


public interface PlottableStorage {
    
    public List<PlottableChunk> get(MicroSecondTimeRange requestRange,
                                    ChannelId channel,
                                    int pixelsPerDay);
    

    public void put(List<PlottableChunk> chunks);
    
    public void commit();
    
    public void rollback();
}
