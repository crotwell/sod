#!/bin/sh
process(){
    input=$1
    output=$2
    style=$3
	echo transforming $input with $style to $output
	xsltproc $style $input | python emptylineDrop.py > $output
}

dropElements(){
    python generateDropperXSLT.py $1 > xslt/dropElements.xslt
    process $2 $3 xslt/dropElements.xslt
    rm xslt/dropElements.xslt
}

dropElements "responseGain integrate rMean rTrend taper legacyExecute" tutorial/waveform.xml demo.xml
process demo.xml vector.xml xslt/waveformToVector.xslt
process demo.xml archive.xml xslt/dataCenterToArchive.xslt
dropElements "someCoverage" demo.xml tutorial/subsettingWaveform.xml
dropElements "distanceRange" tutorial/subsettingWaveform.xml tutorial/simpleWaveform.xml
dropElements "waveformArm networkArm" tutorial/waveform.xml tutorial/event.xml
dropElements "originOR" tutorial/event.xml tutorial/simpleEvent.xml
dropElements "waveformArm eventArm" tutorial/waveform.xml tutorial/network.xml
dropElements "bandCode" tutorial/network.xml tutorial/subsettingNetwork.xml
dropElements "printlineChannelProcessor networkCode" tutorial/subsettingNetwork.xml tutorial/simpleNetwork.xml

