#!/bin/bash
source /etc/bs-tester.conf

MAP=$1
SEED=$2
cd $INSTALL_DIR/repo
cat bc.conf | sed -e 's/bc.game.maps=.*/bc.game.maps='"$MAP"'/' -e 's/bc.game.team-a=.*/bc.game.team-a=old_team_a/' -e 's/bc.game.team-b=.*/bc.game.team-b=old_team_b/' -e 's/bc.server.save-file=.*/bc.server.save-file='"$MAP"'.rms/' > tmp.conf
mv tmp.conf bc.conf
cat maps/$MAP.xml | sed -e 's/seed="[^ ]*"/seed="'"$SEED"'"/' > tmp.xml
mv tmp.xml maps/$MAP.xml
