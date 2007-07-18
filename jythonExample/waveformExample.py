from edu.sc.seis.sod.process.waveform import WaveformProcess, WaveformResult
import edu.sc.seis.sod.status
import sys 

class HelloSod(WaveformProcess):
    def __init__(self, config):
	pass

    def process( self,
		 event,
                 channel,
                 original,
                 available,
                 seismograms,
                 cookieJar):
	print "Hello Sod"
	return WaveformResult(True, seismograms, self)
