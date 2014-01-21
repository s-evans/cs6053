#!/bin/bash

if [ -e /tmp/iwars.ac.pid ]; then
	kill `cat /tmp/iwars.ac.pid`
	rm -f /tmp/iwars.ac.pid
fi

if [ -e /tmp/iwars.ps.pid ]; then
	kill `cat /tmp/iwars.ps.pid`
	rm -f /tmp/iwars.ps.pid
fi

if [ -e /tmp/iwars.rmi1.pid ]; then
	kill `cat /tmp/iwars.rmi1.pid`
	rm -f /tmp/iwars.rmi1.pid
fi

if [ -e /tmp/iwars.rmi2.pid ]; then
	kill `cat /tmp/iwars.rmi2.pid`
	rm -f /tmp/iwars.rmi2.pid
fi
