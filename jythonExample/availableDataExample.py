from edu.sc.seis.sod.subsetter.availableData import AvailableDataSubsetter
from edu.sc.seis.sod.status import Pass, Fail
import sys 
import bag

class AvailableSod(AvailableDataSubsetter):
    def __init__(self, config):
	pass

    def accept( self,
		 event,
                 channel,
                 original,
                 available,
                 cookieJar):
	print "Available Sod: "
	for r in available:
	    print bag.wrap(r)
	return Pass(self)
