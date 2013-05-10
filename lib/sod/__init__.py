from edu.sc.seis.sod.process.waveform import WaveformResult, WaveformVectorResult
from edu.sc.seis.sod.subsetter.dataCenter import BestIRISDataCenter

bestIRISDataCenter = BestIRISDataCenter()

__all__ = ['SodRecipe']

class SodRecipe():
    
    
    def acceptOrigin( self, eventAccess,  eventAttr,  preferred_origin):
	return Pass(self)

    def acceptNetwork(self, networkAttr):
	return Pass(self)

    def acceptStation( self, station,  network):
	return Pass(self)

    def acceptChannel( self, channel,  network):
	return Pass(self)

    def acceptEventStation( event,
			    station,
			    cookieJar):
	return Pass(self)

    def acceptEventChannel( event,
			    channel,
			    cookieJar):
	return Pass(self)

    def generateRequest( event, channel,  cookieJar):
	return []

    def acceptRequest( event,
		       channel,
		       request,
		       cookieJar):
	return Pass(self)

    def getSeismogramDC( event, channel, infilters,  cookieJar):
	return bestIRISDataCenter(event, channel, infilters, cookieJar)

    def acceptAvailableData( self,
			     event,
			     channel,
			     original,
			     available,
			     cookieJar):
	return Pass(self)

    def processSeismograms( self,
			    event,
			    channel,
			    original,
			    available,
			    seismograms,
			    cookieJar):
	return WaveformResult(seismograms, Pass(self))

# vector

    def acceptEventChannelGroup( event,
			    channelGroup,
			    cookieJar):
	return Pass(self)

    def generateVectorRequest( event, channelGroup,  cookieJar):
	return []

    def acceptVectorRequest( event,
		       channelGroup,
		       request,
		       cookieJar):
	return Pass(self)

    def acceptVectorAvailableData( self,
			     event,
			     channel,
			     original,
			     available,
			     cookieJar):
	return Pass(self)


    def processVector(self, event,
		      channelGroup,
		      original,
		      available,
		      seismograms,
		      cookieJar):
	return WaveformVectorResult(seismograms, Pass(self))
