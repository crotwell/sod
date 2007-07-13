from edu.sc.seis.sod.subsetter.site import SiteSubsetter
from edu.sc.seis.sod.status import Pass, Fail

class SiteExample(SiteSubsetter):
    def __init__(self, config):
	pass

    def accept( self, site,  network):
	print 'Got an site: %s'%(site.get_code())
	return Pass(self)
