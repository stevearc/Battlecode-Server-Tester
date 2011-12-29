#!/bin/bash
DIR=$1
TARGET_TEAM=$2
JARFILE=$3
START=`pwd`
cd $DIR
rm -rf tmp
mkdir tmp
pushd tmp > /dev/null

jar xf $START/$JARFILE
find team* -name '*.java' | xargs sed -i -e 's/package team[0-9]\{3\}/package '$TARGET_TEAM'/g' -e 's/import team[0-9]\{3\}/import '$TARGET_TEAM'/g'
mv team* $TARGET_TEAM

cp $START/scripts/build.xml .
ant dist -Dteam=$TARGET_TEAM
mv runnable.jar ../lib/$TARGET_TEAM.jar

popd > /dev/null
rm -rf tmp
