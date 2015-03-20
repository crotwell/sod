package edu.sc.seis.sod.source.seismogram;

import java.util.List;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;


public interface PromiseSeismogramSource extends SeismogramSource {

    public PromiseSeismogramList promiseRetrieveData(List<RequestFilter> request);
    
    public List<PromiseSeismogramList> promiseRetrieveDataList(List<List<RequestFilter>> request);
}
