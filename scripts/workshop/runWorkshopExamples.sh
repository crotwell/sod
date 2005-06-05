#!/bin/sh
log(){ echo `date +%F\ %R:%S` "$@" ; }
files=slides
if [ $# -gt 0 ] ; then 
    files=$1
fi
export SOD_HOME=`cygpath -wa .`
export PATH="$PATH:$SOD_HOME/bin"
for strategy in `cat $files` ; do
    if [ -d $strategy ] ; then 
        log running $strategy
        cd $strategy
        mkdir results
        cd results
        sod.bat -f ../${strategy}.xml > system.out
        log `wc -l Sod_Error.log`
        cd ../../
    else
        echo $strategy not found
    fi
done
