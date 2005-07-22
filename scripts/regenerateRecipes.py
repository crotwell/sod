#! /usr/bin/python -O
import sys
import re
import os
blanks = re.compile('^\s*$')
verbose = False

def generateDropperXSLT(droppedElements):
    return """<?xml version="1.0"?>
    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
        <xsl:output method="xml"/>
        <xsl:include href="copyAll.xslt"/>
        <xsl:template match="%s"/>
    </xsl:stylesheet>""" % ' | '.join(droppedElements)

def dropEmptyLines(instream, outstream):
    [outstream.write(line) for line in instream if not blanks.match(line)]

def process(infile, style, outfile):
    if verbose:
        print 'Transforming %s with %s to %s' % (infile, style, outfile)
    childin, childout = os.popen2("xsltproc %s %s" % (style, infile))
    dropEmptyLines(childout, open(outfile, 'w'))

def dropElements(toDrop, infile, outfile):
    open('xslt/dropper.xslt','w').write(generateDropperXSLT(toDrop))
    process(infile, 'xslt/dropper.xslt', outfile)
    os.remove('xslt/dropper.xslt')

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == '-v': 
        verbose = True
    dropElements(['responseGain', 'integrate', 'rMean', 'rTrend', 'legacyExecute', 'saveSeismogramToFile[dataDirectory]'], 
            'tutorial/waveform.xml', 
            'demo.xml')
    process('demo.xml', 
            'xslt/waveformToVector.xslt', 
            'vector.xml')
    process('demo.xml', 
            'xslt/waveformToVector.xslt', 
            'vector.xml')
    process('demo.xml', 
            'xslt/dataCenterToArchive.xslt', 
            'archive.xml')
    dropElements(["someCoverage"], 
            'demo.xml', 
            'tutorial/subsettingWaveform.xml')
    dropElements(["distanceRange"], 
            'tutorial/subsettingWaveform.xml', 
            'tutorial/simpleWaveform.xml')
    process('tutorial/simpleWaveform.xml',
            'xslt/elideNetworkAndEventArms.xslt',
            'tutorial/incompleteWaveform.xml')
    dropElements(["waveformArm", "networkArm"], 
                'tutorial/waveform.xml', 
                'tutorial/event.xml')
    process('tutorial/event.xml', 
            'xslt/insertPrintlines.xslt',
            'tutorial/booleanPrinterEvent.xml')
    dropElements(["originOR"], 
            'tutorial/event.xml', 
            'tutorial/simpleEvent.xml')
    dropElements(["waveformArm", "eventArm"], 
            'tutorial/waveform.xml', 
            'tutorial/network.xml')
    dropElements(["bandCode"], 
            'tutorial/network.xml', 
            'tutorial/subsettingNetwork.xml')
    process('tutorial/subsettingNetwork.xml',
            'xslt/badNetwork.xslt',
            'tutorial/badNetwork.xml'),
    dropElements(["printlineChannelProcessor"], 
            'tutorial/subsettingNetwork.xml', 
            'tutorial/simpleNetwork.xml')


