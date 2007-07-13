from edu.sc.seis.sod.subsetter.origin import OriginSubsetter
from edu.sc.seis.sod.status import Pass, Fail

class OriginExample(OriginSubsetter):
    def __init__(self, config):
	pass

    def accept( self, eventAccess,  eventAttr,  preferred_origin):
	print 'Got an origin at: %s'%(preferred_origin.getTime())
	return Pass(self)
