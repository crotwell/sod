#!/bin/sh
REPOSITORY=${HOME}/repository

echo writing schema
java -cp `./classpath_creator.sh -seperator \: ` com.sun.msv.writer.relaxng.Driver sod.rng > ../src/edu/sc/seis/sod/data/sod.rng 

java -cp `./classpath_creator.sh -seperator \: ` com.sun.msv.writer.relaxng.Driver network/grouper.rng > ../src/edu/sc/seis/sod/data/grouper.rng 

echo serializing element nodes
java -cp `./classpath_creator.sh -seperator \: ` edu.sc.seis.sod.editor.SchemaGrammar 

echo Done.
