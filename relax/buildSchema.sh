#!/bin/sh
REPOSITORY=${HOME}/.maven/repository

echo writing schema
java -cp `./classpath_creator.sh -seperator \: ` com.sun.msv.writer.relaxng.Driver sod.rng > ../src/edu/sc/seis/sod/data/sod.rng 

echo serializing element nodes
java -cp `./classpath_creator.sh -seperator \: ` edu.sc.seis.sod.editor.SchemaGrammar 

echo Done.
