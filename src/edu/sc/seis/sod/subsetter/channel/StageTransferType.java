package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class StageTransferType  implements ChannelSubsetter {

    public StringTree accept(Channel channel, ProxyNetworkAccess network) {
        try {
            Instrumentation inst = network.retrieve_instrumentation(channel.get_id(),
                                                            channel.get_id().begin_time);
            return new StringTreeLeaf(this, type.equals(inst.the_response.stages[stageNum-1].type));
        } catch(ChannelNotFound e) {
            return new Fail(this, "No instrumentation");
        } catch (InstrumentationInvalid e) {
            return new Fail(this, "Invalid instrumentation");
        }
    }

    TransferType type;
    
    int stageNum;
}
