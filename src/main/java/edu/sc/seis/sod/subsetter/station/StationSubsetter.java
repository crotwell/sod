package edu.sc.seis.sod.subsetter.station;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * StationSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:05:33 2001
 *
 * @author Philip Crotwell
 */

public interface StationSubsetter extends Subsetter{

    public StringTree accept(StationImpl station, NetworkSource network) throws Exception;

}// StationSubsetter
