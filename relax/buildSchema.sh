#!/bin/sh
REPOSITORY=${HOME}/repository

echo writing schema
java -cp `./classpath_creator.sh -seperator : -repository ${REPOSITORY}` com.sun.msv.writer.relaxng.Driver sod.rng > ../src/edu/sc/seis/sod/data/sod.rng 

echo Done.
