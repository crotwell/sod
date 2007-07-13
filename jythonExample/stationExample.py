from edu.sc.seis.sod.subsetter.station import StationSubsetter
from edu.sc.seis.sod.status import Pass, Fail

class StationExample(StationSubsetter):
    def __init__(self, config):
	pass

    def accept( self, station,  network):
	print 'Got an station: %s'%(station.get_code())
	if station.get_code() == 'RPN':
	    return Pass(self)
	return Fail(self, 'Station code is not SNZO')
