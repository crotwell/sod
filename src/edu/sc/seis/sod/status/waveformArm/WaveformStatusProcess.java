package edu.sc.seis.sod.status.waveFormArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.Subsetter;
import edu.sc.seis.sod.database.Status;


/**
 * WaveformStatusProcess.java
 *
 *
 * Created: Fri Oct 18 14:57:48 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public interface WaveformStatusProcess extends Subsetter{

    public void begin(EventAccessOperations eventAccess) throws Exception;

    public void begin(EventAccessOperations eventAccess,
              NetworkAccess networkAccess) throws Exception;

    public void begin(EventAccessOperations eventAccess,
              Station station) throws Exception;

    public void begin(EventAccessOperations eventAccess,
              Site site) throws Exception;

    public void begin(EventAccessOperations eventAccess,
              Channel channel) throws Exception;

    public void end(EventAccessOperations eventAccess,
            Channel channel,
            Status status,
            String reason) throws Exception;

    public void end(EventAccessOperations eventAccess,
            Site site) throws Exception;

    public void end(EventAccessOperations eventAccess,
            Station station) throws Exception;

    public void end(EventAccessOperations eventAccess,
            NetworkAccess networkAccess) throws Exception;

    public void end(EventAccessOperations eventAccess) throws Exception;

    public void closeProcessing() throws Exception;

}// WaveformStatusProcess
