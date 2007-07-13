from edu.sc.seis.sod.subsetter.network import NetworkSubsetter
from edu.sc.seis.sod.status import Pass, Fail

class NetworkExample(NetworkSubsetter):
    def __init__(self, config):
	pass

    def accept(self, networkAttr):
	print 'Got an network: %s'%(networkAttr.get_code())
	if networkAttr.get_code() == 'II':
	    return Pass(self)
	return Fail(self, 'Network code is not II')
