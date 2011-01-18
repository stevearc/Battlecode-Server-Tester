#!/bin/bash
source /etc/bs-tester.conf

REPO=$1
MAP=$2
SEED=$3
cd $REPO
sed -i -e 's/bc.game.maps=.*/bc.game.maps='"$MAP"'/' -e 's/bc.game.team-a=.*/bc.game.team-a=old_team_a/' -e 's/bc.game.team-b=.*/bc.game.team-b=old_team_b/' -e 's/bc.server.save-file=.*/bc.server.save-file='"$MAP"'.rms/' bc.conf
sed -i -e 's/seed="[^ ]*"/seed="'"$SEED"'"/' maps/$MAP.xml
