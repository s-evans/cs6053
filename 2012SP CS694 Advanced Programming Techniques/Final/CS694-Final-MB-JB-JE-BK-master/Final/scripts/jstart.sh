#!/bin/bash

IWARS_USER=mjb10
IWARS_MON=helios.ececs.uc.edu
IWARS_MON_PORT=8180
IWARS_SERVER_PORT=22556

if [ $1 ]; then IWARS_SERVER_PORT=$1; fi

echo "Using server port $IWARS_SERVER_PORT..."

# avoid zombies.  ensure everything is stopped.
`dirname $0`/stop.sh

# tunnel for active client
xterm -e ssh -vL $IWARS_MON_PORT:$IWARS_MON:$IWARS_MON_PORT $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.ac.pid

# tunnel for passive server
xterm -e ssh -vR $IWARS_SERVER_PORT:localhost:$IWARS_SERVER_PORT $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.ps.pid

# tunnel for RMI
xterm -e ssh -vL 1098:$IWARS_MON:1098 $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.rmi1.pid

# tunnel for RMI
xterm -e ssh -vL 1099:localhost:1099 $IWARS_USER@$IWARS_MON &
echo "$!" > /tmp/iwars.rmi2.pid

