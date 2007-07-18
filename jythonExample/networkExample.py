from edu.sc.seis.sod.subsetter.network import NetworkSubsetter
from edu.sc.seis.sod.status import Pass, Fail

class NetworkExample(NetworkSubsetter):
    "Simple example to check the network code"

    def __init__(self, config):
	self.networkCode = 'II'
	pass

    def accept(self, networkAttr):
	print 'Got an network: %s'%(networkAttr.get_code())
	if networkAttr.get_code() == self.networkCode:
	    return Pass(self)
	return Fail(self, 'Network code is not %s'%(self.networkCode))
