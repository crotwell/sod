#!/bin/sh
REPOSITORY=${HOME}/repository
java -cp `./classpath_creator.sh -seperator : -repository ${REPOSITORY}` edu.sc.seis.sod.Start -props cwg.prop $* 
