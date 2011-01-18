#!/bin/bash
source /etc/bs-tester.conf

REPO=$1
USER=`ls -ld $REPO | awk '{split($0,a," ")} END{print a[3]}'`
CMD_PREFIX="sudo -u $USER"

cd $REPO
$CMD_PREFIX hg revert -a > /dev/null
$CMD_PREFIX hg up tip > /dev/null
$CMD_PREFIX hg pull > /dev/null
$CMD_PREFIX hg update > /dev/null
chown -R $USER .
hg tags | cut -d " " -f -5
