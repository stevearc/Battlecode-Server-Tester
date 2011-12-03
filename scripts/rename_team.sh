#!/bin/bash
if [ "$1" == "" ]; then
    echo "Usage: $0 target_team file.jar"
    exit 1
fi
if [ "$2" == "" ]; then
    echo "Usage: $0 target_team file.jar"
    exit 1
fi

TARGET_TEAM=$1
FILE=$2

rm -rf team*
rm -rf $TARGET_TEAM

jar -x -f $FILE
find team* -name '*.java' | xargs sed -i -e 's/package team[0-9]\{3\}/package '$TARGET_TEAM'/g'
find team* -name '*.java' | xargs sed -i -e 's/import team[0-9]\{3\}/import '$TARGET_TEAM'/g'

mv team* $TARGET_TEAM

jar -c -f $TARGET_TEAM.jar $TARGET_TEAM
rm -rf $TARGET_TEAM
rm -rf META-INF
