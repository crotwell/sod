package edu.sc.seis.sod.source.seismogram;

import java.util.List;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.SodElement;


public interface SeismogramSource extends SodElement {
    
    public List<RequestFilter> availableData(List<RequestFilter> request) throws SeismogramSourceException;
    
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException;
    
}
