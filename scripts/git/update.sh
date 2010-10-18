#!/bin/bash
if [ -e /etc/battlecode.conf ]; then
	source /etc/battlecode.conf
else
	source etc/battlecode.conf
fi

USER=`ls -ld $REPO | awk '{split($0,a," ")} END{print a[3]}'`
CMD_PREFIX="sudo -u $USER"

cd $REPO
$CMD_PREFIX git reset --hard > /dev/null
$CMD_PREFIX git checkout -f master > /dev/null
$CMD_PREFIX git pull > /dev/null
chown -R $USER .
git tag
