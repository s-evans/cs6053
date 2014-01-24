#!/bin/bash

for FILE in /tmp/netsec.ac.pid /tmp/netsec.ps.pid; do
    if [ -e $FILE ]; then
        PID=`cat $FILE`
    	echo Killing PID $PID...
    	kill $PID
    	rm -fv $FILE
    fi
done
