from edu.sc.seis.sod.subsetter.station import StationSubsetter
from edu.sc.seis.sod.status import Pass, Fail
from edu.sc.seis.sod import SodUtil

class StationExample(StationSubsetter):
    def __init__(self, config):
	self.stationCode = SodUtil.getText(SodUtil.getElement(config, 'stationCode'))
	if self.stationCode == '':
	    self.stationCode = 'SNZO'
	pass

    def accept( self, station,  network):
	print 'Got an station: %s'%(station.get_code())
	if station.get_code() == self.stationCode:
	    return Pass(self)
	return Fail(self, 'Station code is not %s'%(self.stationCode))
