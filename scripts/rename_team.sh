#!/bin/bash
DIR=$1
TARGET_TEAM=$2
JARFILE=$3
START=`pwd`
cd $DIR
rm -rf tmp
mkdir tmp
pushd tmp > /dev/null

if [ -e $START/teams/$TARGET_TEAM ]; then
    # Don't need to do this.
    exit
fi

jar xf $START/$JARFILE
find team* -name '*.java' | xargs sed -i -e 's/package team[0-9]\{3\}/package '$TARGET_TEAM'/g' -e 's/import team[0-9]\{3\}/import '$TARGET_TEAM'/g'
mv team* $TARGET_TEAM

ln -s $START/scripts/build.xml .

ant compile -Dteam=$TARGET_TEAM
mv build/$TARGET_TEAM $START/teams

popd > /dev/null
rm -rf tmp
