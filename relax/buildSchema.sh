#!/bin/sh
python schemaBuilderScriptBuilder.py
sh bigSchemaBuilder.sh
echo Done.  If you're not going to monkey with the schema any more you'll want to commit elementNode.ser, sod.rng, and grouper.rng in src/edu/sc/seis/sod/data.  If not, I'll come complain to you.  If I'm reading this message at a later date I'll want to commit elementNode.ser, sod.rng and grouper.rng in src/edu/sc/seis/sod/data so that someone else doesn't come to complain to me.  
