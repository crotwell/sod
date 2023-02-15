package edu.sc.seis.sod.hibernate;

import java.util.List;

import edu.sc.seis.sod.model.common.TimeRange;
import edu.sc.seis.sod.model.seismogram.PlottableChunk;
import edu.sc.seis.sod.model.station.ChannelId;


public interface PlottableStorage {
    
    public List<PlottableChunk> get(TimeRange requestRange,
                                    ChannelId channel,
                                    int pixelsPerDay);
    

    public void put(List<PlottableChunk> chunks);
    
    public void commit();
    
    public void rollback();
}