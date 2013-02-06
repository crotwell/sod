package edu.sc.seis.sod.source.seismogram;

import java.util.List;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.seisFile.dataSelectWS.DataSelectException;
import edu.sc.seis.sod.SodElement;


public interface SeismogramSource extends SodElement {
    
    public List<RequestFilter> available_data(List<RequestFilter> request);
    
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException;
    
}
