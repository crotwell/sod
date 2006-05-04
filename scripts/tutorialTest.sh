#!/bin/sh
run(){
    mkdir $1
    cd $1
    echo $2
    echo $1
    sod $2 > ../$1.out
    cd ../
}
runrecipe(){
    run $1 "-f ${SOD_HOME}/recipes/tutorial/$1.xml"
}

VERSION=2.1.2beta
DIR=sod-${VERSION}
sod.py -o . --tar
tar xzf ${DIR}.tar.gz
export SOD_HOME=`pwd`/${DIR}
echo $SOD_HOME
export PATH=${SOD_HOME}/bin:$PATH
runrecipe waveform
run demo -demo
runrecipe network
runrecipe event
runrecipe simpleNetwork
runrecipe subsettingNetwork
runrecipe simpleEvent
runrecipe simpleWaveform
runrecipe subsettingWaveform
