#!/bin/sh

OB=../../OrbacusSignedJars/dist/OB.jar
OBNAMING=../../OrbacusSignedJars/dist/OBNaming.jar
FISSURESUTIL=../../fissuresUtil/dist/lib/fissuresUtil.jar
FISSURESIMPL=../../fissures/dist/lib/FissuresImpl.jar
FISSURESIDL=../../fissures/dist/lib/FissuresIDL.jar


java -cp ../build:${FISSURESIDL}:${FISSURESIMPL}:${FISSURESUTIL}:${OB}:${OBNAMING} edu.sc.seis.sod.Start $*
