#!/bin/bash
source /etc/bs-tester.conf

USER=`ls -ld $REPO | awk '{split($0,a," ")} END{print a[3]}'`
CMD_PREFIX="sudo -u $USER"

cd $REPO
$CMD_PREFIX git reset --hard > /dev/null
$CMD_PREFIX git checkout -f master > /dev/null
$CMD_PREFIX git pull > /dev/null
chown -R $USER .
git tag
