#!/bin/sh
REPOSITORY=${HOME}/repository

echo writing schema
java -cp `./classpath_creator.sh -seperator \: ` com.sun.msv.writer.relaxng.Driver sod.rng > ../src/edu/sc/seis/sod/data/sod.rng 

java -cp `./classpath_creator.sh -seperator \: ` com.sun.msv.writer.relaxng.Driver network/grouper.rng > ../src/edu/sc/seis/sod/data/grouper.rng 

echo serializing element nodes
java -cp `./classpath_creator.sh -seperator \: ` edu.sc.seis.sod.editor.SchemaGrammar 

echo Done.  If you're not going to monkey with the schema any more you'll want to commit elementNode.ser, sod.rng, and grouper.rng in src/edu/sc/seis/sod/data.  If not, I'll come complain to you.  If I'm reading this message at a later date I'll want to commit elementNode.ser, sod.rng and grouper.rng in src/edu/sc/seis/sod/data so that someone else doesn't come to complain to me.  
