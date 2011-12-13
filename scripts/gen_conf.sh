#!/bin/bash
RUNDIR=$1
MAP=$2
TEAM_A=$3
TEAM_B=$4
SEED=$5

if [ ! -e $RUNDIR ]; then
    mkdir -p $RUNDIR
    cd $RUNDIR
    ln -s ../build.xml ../idata ../teams .
    cp -pr ../bc.conf ../maps .
fi

cd $RUNDIR
# Make sure we have the most up-to-date battlecode version
rm -rf lib
cp -pr ../lib .

sed -i -e 's/bc.game.maps=.*/bc.game.maps='"$MAP"'/' -e 's/bc.game.team-a=.*/bc.game.team-a='$TEAM_A'/' -e 's/bc.game.team-b=.*/bc.game.team-b='$TEAM_B'/' -e 's/bc.server.save-file=.*/bc.server.save-file='"$MAP"'.rms/' bc.conf
sed -i -e 's/seed="[^ ]*"/seed="'"$SEED"'"/' maps/$MAP.xml
