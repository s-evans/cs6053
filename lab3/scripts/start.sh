#!/bin/bash

IWARS_MON=helios.ececs.uc.edu
IWARS_MON_PORT=8180
if [ $1 ]; then IWARS_USER=$1; else IWARS_USER=elchisjm; fi
if [ $2 ]; then IWARS_SERVER_PORT=$2; else IWARS_SERVER_PORT=20000; fi

echo "Using username $IWARS_USER..."
echo "Using server port $IWARS_SERVER_PORT..."

# avoid zombies.  ensure everything is stopped.
`dirname $0`/stop.sh

# tunnel for active client
xterm -e ssh -vL $IWARS_MON_PORT:$IWARS_MON:$IWARS_MON_PORT $IWARS_USER@$IWARS_MON &
echo "$!" | tee /tmp/netsec.ac.pid

# tunnel for passive server
xterm -e ssh -vR $IWARS_SERVER_PORT:localhost:$IWARS_SERVER_PORT $IWARS_USER@$IWARS_MON &
echo "$!" | tee /tmp/netsec.ps.pid
