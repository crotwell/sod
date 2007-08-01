from edu.sc.seis.sod.process.waveform import WaveformProcess, WaveformResult
from edu.iris.Fissures.seismogramDC import LocalSeismogramImpl
import edu.sc.seis.sod.status
import sys 
from jarray import array
from bag import *

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
	print "Hello Sod "
	out = array([], LocalSeismogramImpl)
	for s in seismograms:
	    s = rmean(s)
	    s = taper(rtrend(s))
	    print s
	    print
	    print stat(s)
	    print
	    out.append(s)
	return WaveformResult(True, out, self)
