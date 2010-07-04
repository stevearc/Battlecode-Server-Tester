#!/bin/bash
if [ -e /etc/battlecode.conf ]; then
	source /etc/battlecode.conf
else
	source etc/battlecode.conf
fi
MAP=$1
cat etc/bc.conf.template | sed -e 's/map_goes_here/'"$MAP"'/' > $REPO/bc.conf
