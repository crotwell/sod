package edu.sc.seis.sod.status;



import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.mockFissures.IfNetwork.MockChannel;
import edu.sc.seis.sod.XMLConfigUtil;
import java.text.SimpleDateFormat;
import junit.framework.TestCase;

public class ChannelFormatterTest extends TestCase{
    public ChannelFormatterTest(String name){ super(name); }
    
    public void setUp(){
        chan = MockChannel.createChannel();
    }
    
    public void testStationCode(){
        assertEquals(chan.get_id().station_code, create("<stationCode/>").getResult(chan));
    }
    
    public void testNetworkCode(){
        assertEquals(chan.get_id().network_id.network_code,
                     create("<networkCode/>").getResult(chan));
    }
    
    public void testSiteCode(){
        assertEquals(chan.get_id().site_code, create("<siteCode/>").getResult(chan));
    }
    
    public void testBeginTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        ChannelFormatter cf = create("<beginTime>HH:mm</beginTime>");
        assertEquals(sdf.format(new MicroSecondDate(chan.get_id().begin_time)),
                     cf.getResult(chan));
    }
    
    public void testName(){
        assertEquals("Test Channel", create("<name/>").getResult(chan));
    }
    
    private ChannelFormatter create(String config){
        try {
            return new ChannelFormatter(XMLConfigUtil.parse(open + config + close));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String open = "<channelFormat>";
    
    private String close = "</channelFormat>";
    
    private Channel chan;
}
