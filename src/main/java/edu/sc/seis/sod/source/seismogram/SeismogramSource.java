package edu.sc.seis.sod.source.seismogram;

import java.util.List;

import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;


public interface SeismogramSource extends SodElement {
    
    public List<RequestFilter> availableData(List<RequestFilter> request) throws SeismogramSourceException;
    
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException;
    
}
