#!/bin/sh
sod.py --docs
seiswww.py
cd ../../seiswww/output
./bin/sodDocs
sod.py --release --tar --zip 
