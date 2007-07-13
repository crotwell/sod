from edu.sc.seis.sod.subsetter.channel import ChannelSubsetter
from edu.sc.seis.sod.status import Pass, Fail

class ChannelCodeIsBHZ(ChannelSubsetter):
    def __init__(self, config):
	pass

    def accept( self, channel,  network):
	print 'Got an channel: %s'%(channel.get_code())
	if channel.get_code() == 'BHZ':
	    return Pass(self)
	return Fail(self, 'Station code is not BHZ')
