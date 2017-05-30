package edu.sc.seis.sod.source.seismogram;

import java.util.List;

import edu.sc.seis.sod.model.seismogram.RequestFilter;


public interface PromiseSeismogramSource extends SeismogramSource {

    public PromiseSeismogramList promiseRetrieveData(List<RequestFilter> request);
    
    public List<PromiseSeismogramList> promiseRetrieveDataList(List<List<RequestFilter>> request);
}
