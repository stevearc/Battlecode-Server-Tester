#!/bin/bash
if [ -e /etc/battlecode.conf ]; then
	source /etc/battlecode.conf
else
	source etc/battlecode.conf
fi

CMD_PREFIX=""
CUR_USER=`whoami`
if [ "$CUR_USER" != "$USER" ]; then
	CMD_PREFIX="sudo -u $USER"
fi

pushd $REPO
TEAM_A=$1
TEAM_B=$2
$CMD_PREFIX git reset --hard
# Get team a
rm -rf teams/old_team_a
$CMD_PREFIX git checkout -f $TEAM_A
pushd teams
cp -r $TEAM old_team_a
cd old_team_a
find -name '*.java' | xargs sed -i -e 's/package '"$TEAM"'/package old_team_a/g'
find -name '*.java' | xargs sed -i -e 's/import '"$TEAM"'/import old_team_a/g'
popd

# Get team b
rm -rf teams/old_team_b
$CMD_PREFIX git checkout -f $TEAM_B
pushd teams
cp -r $TEAM old_team_b
cd old_team_b
find -name '*.java' | xargs sed -i -e 's/package '"$TEAM"'/package old_team_b/g'
find -name '*.java' | xargs sed -i -e 's/import '"$TEAM"'/import old_team_b/g'
popd
$CMD_PREFIX git checkout -f master

popd
